package com.focuslock.dns.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.focuslock.dns.data.FocusLockPreferences
import com.focuslock.dns.timer.LockTimerManager
import com.focuslock.dns.vpn.FocusLockVpnService
import com.focuslock.dns.widget.FocusLockWidgetProvider

/**
 * Restores timer schedules and restarts FocusLockVpnService after device reboot
 * whenever Android permits foreground VPN start and VPN consent remains granted.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val prefs = FocusLockPreferences(context)
        val timerManager = LockTimerManager(context, prefs)
        timerManager.restoreScheduleIfNeeded()
        FocusLockWidgetProvider.requestUpdate(context)

        if (!prefs.shouldMaintainVpn) return

        val vpnConsentMissing = VpnService.prepare(context) != null
        if (vpnConsentMissing) {
            FocusLockVpnService.postBootResumeNotification(context)
            return
        }

        runCatching {
            FocusLockVpnService.startOrSync(context)
        }.onFailure {
            // Android / MyOS 13 background restrictions may block FGS start; show prompt notification
            FocusLockVpnService.postBootResumeNotification(context)
        }
    }
}
