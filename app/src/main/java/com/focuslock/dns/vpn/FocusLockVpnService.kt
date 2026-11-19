package com.focuslock.dns.vpn

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.focuslock.dns.FocusLockApplication
import com.focuslock.dns.MainActivity
import com.focuslock.dns.R
import com.focuslock.dns.data.FocusLockPreferences
import com.focuslock.dns.data.MotivationalReminders
import com.focuslock.dns.model.DnsProfile
import com.focuslock.dns.model.VpnConnectionState
import com.focuslock.dns.model.VpnRuntimeSnapshot
import com.focuslock.dns.ui.BlockReminderActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Single local VpnService DNS filtering engine supporting both IPv4 & IPv6.
 * - Normal Mode: https://dns.adguard-dns.com/dns-query (AdGuard Default)
 * - Focus Lock Mode: https://family.adguard-dns.com/dns-query (AdGuard Family)
 */
class FocusLockVpnService : VpnService() {

    private lateinit var preferences: FocusLockPreferences
    private var dohClient: DohResolverClient? = null
    private var tunDescriptor: ParcelFileDescriptor? = null
    private var tunOutputStream: FileOutputStream? = null
    private val outputWriteLock = Any()

    private val isRunning = AtomicBoolean(false)
    private val activeProfileRef = AtomicReference(DnsProfile.NORMAL)
    private var readerThread: Thread? = null
    private var serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val querySemaphore = Semaphore(24)
    private val lastReminderShownAtMs = AtomicLong(0L)
    private val reminderQuoteCursor = AtomicInteger(0)

    override fun onCreate() {
        super.onCreate()
        preferences = FocusLockPreferences(applicationContext)
        dohClient = DohResolverClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_OR_SYNC

        if (action == ACTION_STOP_IF_UNLOCKED) {
            if (!preferences.readTimerState().isFocusLockActive) {
                preferences.shouldMaintainVpn = false
                teardownTunnel(VpnConnectionState.DISCONNECTED, null)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val desiredProfile = preferences.resolveDesiredProfile()
        activeProfileRef.set(desiredProfile)

        startForeground(NOTIFICATION_ID_VPN, buildStatusNotification(desiredProfile))

        if (!isRunning.get() || tunDescriptor == null) {
            preferences.shouldMaintainVpn = true
            startTunnel(desiredProfile)
        } else {
            updateRuntimeSnapshot(VpnConnectionState.ACTIVE, desiredProfile, null)
            refreshNotification(desiredProfile)
        }

        return START_STICKY
    }

    private fun startTunnel(initialProfile: DnsProfile) {
        updateRuntimeSnapshot(VpnConnectionState.CONNECTING, initialProfile, null)

        val builder = Builder()
            .setSession("FocusLock DNS Filter")
            .setMtu(1500)
            .addAddress(VPN_IPV4_CLIENT, 32)
            .addDnsServer(VPN_IPV4_DNS)
            .addRoute(VPN_IPV4_DNS, 32)
            .addAddress(VPN_IPV6_CLIENT, 128)
            .addDnsServer(VPN_IPV6_DNS)
            .addRoute(VPN_IPV6_DNS, 128)

        runCatching { builder.addDisallowedApplication(packageName) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        val pfd = runCatching { builder.establish() }.getOrElse { err ->
            updateRuntimeSnapshot(
                VpnConnectionState.ERROR,
                initialProfile,
                err.localizedMessage ?: "Failed to establish VPN interface"
            )
            stopSelf()
            return
        }

        if (pfd == null) {
            updateRuntimeSnapshot(
                VpnConnectionState.REVOKED,
                initialProfile,
                "VPN permission revoked"
            )
            stopSelf()
            return
        }

        tunDescriptor = pfd
        tunOutputStream = FileOutputStream(pfd.fileDescriptor)
        isRunning.set(true)
        updateRuntimeSnapshot(VpnConnectionState.ACTIVE, initialProfile, null)

        readerThread = Thread(::runTunReadLoop, "FocusLock-TUN-Reader").apply {
            priority = Thread.NORM_PRIORITY
            start()
        }
    }

    private fun runTunReadLoop() {
        val pfd = tunDescriptor ?: return
        val inputStream = FileInputStream(pfd.fileDescriptor)
        val buffer = ByteArray(4096)

        try {
            while (isRunning.get() && !Thread.currentThread().isInterrupted) {
                val length = inputStream.read(buffer)
                if (length <= 0) continue

                syncProfileFromTimerIfNeeded()

                val parsed = TunPacketCodec.parseDnsPacket(buffer, length) ?: continue
                val currentProfile = activeProfileRef.get()

                serviceScope.launch {
                    querySemaphore.withPermit {
                        handleDnsPacket(parsed, currentProfile)
                    }
                }
            }
        } catch (_: InterruptedException) {
            // Normal shutdown
        } catch (err: Exception) {
            if (isRunning.get()) {
                updateRuntimeSnapshot(
                    VpnConnectionState.ERROR,
                    activeProfileRef.get(),
                    err.localizedMessage
                )
            }
        }
    }

    private fun handleDnsPacket(parsed: TunPacketCodec.ParsedDnsPacket, profile: DnsProfile) {
        if (!isRunning.get()) return
        val client = dohClient ?: return

        val dohAnswer = client.resolveDnsQuery(profile, parsed.dnsPayload)

        if (dohAnswer != null &&
            profile == DnsProfile.FOCUS_LOCK &&
            MotivationalReminders.isBlockedDnsResponse(dohAnswer) &&
            MotivationalReminders.isAdultCategoryQueryInRam(parsed.dnsPayload)
        ) {
            onAdultBlockDetectedInRam()
        }

        val responseDns = dohAnswer ?: TunPacketCodec.buildServFailDnsResponse(parsed.dnsPayload)
        if (responseDns.isEmpty()) return

        val responsePacket = TunPacketCodec.buildResponsePacket(parsed, responseDns)
        val out = tunOutputStream ?: return
        synchronized(outputWriteLock) {
            if (isRunning.get()) {
                runCatching {
                    out.write(responsePacket)
                }
            }
        }
    }

    private fun onAdultBlockDetectedInRam() {
        val now = System.currentTimeMillis()
        val prev = lastReminderShownAtMs.get()
        if (now - prev < REMINDER_COOLDOWN_MS) return
        if (!lastReminderShownAtMs.compareAndSet(prev, now)) return

        // Increment pure integer counter only — zero domain names stored
        preferences.incrementUrgesResisted()

        if (!preferences.motivationalReminderEnabled) return

        val quoteIndex = reminderQuoteCursor.getAndIncrement()
        val quote = MotivationalReminders.quoteForIndex(quoteIndex)
        val isBn = preferences.appLanguage == FocusLockPreferences.LANG_BN

        val reminderIntent = Intent(applicationContext, BlockReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(BlockReminderActivity.EXTRA_QUOTE_INDEX, quoteIndex)
        }

        if (Settings.canDrawOverlays(applicationContext)) {
            runCatching {
                startActivity(reminderIntent)
                return
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID_REMINDER,
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isBn) quote.titleBn else quote.titleEn
        val body = if (isBn) quote.bodyBn else quote.bodyEn

        val notification = NotificationCompat.Builder(
            applicationContext,
            FocusLockApplication.CHANNEL_PROTECTION_ALERTS
        )
            .setSmallIcon(R.drawable.ic_focuslock_shield)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .build()

        getSystemService(NotificationManager::class.java)
            ?.notify(NOTIFICATION_ID_REMINDER, notification)
    }

    private fun syncProfileFromTimerIfNeeded() {
        val desired = preferences.resolveDesiredProfile()
        val current = activeProfileRef.get()
        if (desired != current) {
            if (desired == DnsProfile.NORMAL) {
                preferences.clearExpiredTimer()
            }
            activeProfileRef.set(desired)
            updateRuntimeSnapshot(VpnConnectionState.ACTIVE, desired, null)
            refreshNotification(desired)
        }
    }

    override fun onRevoke() {
        super.onRevoke()
        val timerActive = preferences.readTimerState().isFocusLockActive
        teardownTunnel(VpnConnectionState.REVOKED, "VPN disabled in Android Settings")
        if (timerActive) {
            postRevokedWarningNotification(applicationContext)
        }
        stopSelf()
    }

    override fun onDestroy() {
        val currentState = _runtimeSnapshot.value.connectionState
        val nextState = if (currentState == VpnConnectionState.REVOKED) {
            VpnConnectionState.REVOKED
        } else {
            VpnConnectionState.DISCONNECTED
        }
        teardownTunnel(nextState, null)
        dohClient?.shutdown()
        dohClient = null
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun teardownTunnel(newState: VpnConnectionState, errorMessage: String?) {
        isRunning.set(false)
        readerThread?.interrupt()
        readerThread = null
        runCatching { tunOutputStream?.close() }
        tunOutputStream = null
        runCatching { tunDescriptor?.close() }
        tunDescriptor = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        updateRuntimeSnapshot(newState, preferences.resolveDesiredProfile(), errorMessage)
    }

    private fun refreshNotification(profile: DnsProfile) {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        manager.notify(NOTIFICATION_ID_VPN, buildStatusNotification(profile))
    }

    private fun buildStatusNotification(profile: DnsProfile): Notification {
        val isBn = preferences.appLanguage == FocusLockPreferences.LANG_BN
        val title = when {
            profile == DnsProfile.FOCUS_LOCK && isBn -> getString(R.string.notif_focus_lock_title)
            profile == DnsProfile.FOCUS_LOCK -> "FocusLock — Focus Lock Active"
            isBn -> getString(R.string.notif_normal_title)
            else -> "FocusLock — Normal Protection Active"
        }
        val body = when {
            profile == DnsProfile.FOCUS_LOCK && isBn -> getString(R.string.notif_focus_lock_body)
            profile == DnsProfile.FOCUS_LOCK -> "AdGuard Family DNS (Adult sites, ads & trackers blocked)"
            isBn -> getString(R.string.notif_normal_body)
            else -> "AdGuard Default DNS (Always-on ad & tracker blocking)"
        }

        val launchIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, FocusLockApplication.CHANNEL_VPN_STATUS)
            .setSmallIcon(R.drawable.ic_focuslock_shield)
            .setContentTitle(title)
            .setContentText(body)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(launchIntent)
            .build()
    }

    companion object {
        private const val VPN_IPV4_CLIENT = "10.89.0.1"
        private const val VPN_IPV4_DNS = "10.89.0.2"
        private const val VPN_IPV6_CLIENT = "fd89:0:0:1::1"
        private const val VPN_IPV6_DNS = "fd89:0:0:1::2"

        private const val NOTIFICATION_ID_VPN = 1001
        private const val NOTIFICATION_ID_ALERT = 1002
        private const val NOTIFICATION_ID_REMINDER = 1003
        private const val REMINDER_COOLDOWN_MS = 20_000L

        const val ACTION_START_OR_SYNC = "com.focuslock.dns.action.START_OR_SYNC"
        const val ACTION_STOP_IF_UNLOCKED = "com.focuslock.dns.action.STOP_IF_UNLOCKED"

        private val _runtimeSnapshot = MutableStateFlow(VpnRuntimeSnapshot())
        val runtimeSnapshot: StateFlow<VpnRuntimeSnapshot> = _runtimeSnapshot.asStateFlow()

        private fun updateRuntimeSnapshot(
            state: VpnConnectionState,
            profile: DnsProfile,
            error: String?
        ) {
            _runtimeSnapshot.value = VpnRuntimeSnapshot(
                connectionState = state,
                activeProfile = profile,
                lastError = error
            )
        }

        fun startOrSync(context: Context) {
            val intent = Intent(context, FocusLockVpnService::class.java).apply {
                action = ACTION_START_OR_SYNC
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun syncProfile(context: Context) {
            startOrSync(context)
        }

        fun isSystemPrivateDnsStrict(context: Context): Boolean {
            return runCatching {
                val mode = Settings.Global.getString(context.contentResolver, "private_dns_mode")
                mode == "hostname"
            }.getOrDefault(false)
        }

        fun postRevokedWarningNotification(context: Context) {
            postAlertNotification(
                context,
                context.getString(R.string.notif_revoked_title),
                context.getString(R.string.notif_revoked_body)
            )
        }

        fun postBootResumeNotification(context: Context) {
            postAlertNotification(
                context,
                context.getString(R.string.notif_boot_needed_title),
                context.getString(R.string.notif_boot_needed_body)
            )
        }

        private fun postAlertNotification(context: Context, title: String, body: String) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val launchIntent = PendingIntent.getActivity(
                context,
                1,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(
                context,
                FocusLockApplication.CHANNEL_PROTECTION_ALERTS
            )
                .setSmallIcon(R.drawable.ic_focuslock_shield)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(launchIntent)
                .build()
            manager.notify(NOTIFICATION_ID_ALERT, notification)
        }
    }
}
