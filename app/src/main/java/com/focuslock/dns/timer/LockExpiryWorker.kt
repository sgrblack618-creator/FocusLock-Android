package com.focuslock.dns.timer

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.focuslock.dns.data.FocusLockPreferences
import com.focuslock.dns.vpn.FocusLockVpnService

/**
 * Ensures Focus Lock automatically transitions back to Normal Mode (AdGuard Default)
 * upon timer expiry without ever disabling ad/tracker filtering.
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
            if (prefs.shouldMaintainVpn) {
                FocusLockVpnService.syncProfile(applicationContext)
            }
        }
        return Result.success()
    }
}
