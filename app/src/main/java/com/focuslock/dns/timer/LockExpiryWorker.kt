package com.focuslock.dns.timer

import android.content.Context
import android.net.VpnService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.focuslock.dns.data.FocusLockPreferences
import com.focuslock.dns.model.VpnConnectionState
import com.focuslock.dns.vpn.FocusLockVpnService
import com.focuslock.dns.widget.FocusLockWidgetProvider

/**
 * Dual-purpose background worker:
 * 1. Ensures Focus Lock automatically transitions back to Normal Mode upon expiry.
 * 2. Acts as an anti-kill background watchdog to revive FocusLockVpnService if MyOS 13
 *    reclaimed it under 4GB RAM load while protection is enabled.
 */
class LockExpiryWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = FocusLockPreferences(applicationContext)
        val timerState = prefs.readTimerState()

        if (!timerState.isFocusLockActive) {
            prefs.clearExpiredTimer()
        }

        if (prefs.shouldMaintainVpn && VpnService.prepare(applicationContext) == null) {
            val vpnState = FocusLockVpnService.runtimeSnapshot.value.connectionState
            if (vpnState != VpnConnectionState.ACTIVE && vpnState != VpnConnectionState.REVOKED) {
                runCatching {
                    FocusLockVpnService.startOrSync(applicationContext)
                }.onFailure {
                    FocusLockVpnService.postBootResumeNotification(applicationContext)
                }
            } else if (!timerState.isFocusLockActive) {
                FocusLockVpnService.syncProfile(applicationContext)
            }
        }

        FocusLockWidgetProvider.requestUpdate(applicationContext)
        return Result.success()
    }
}
