package com.focuslock.dns.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.focuslock.dns.data.FocusLockPreferences
import com.focuslock.dns.model.VpnConnectionState
import com.focuslock.dns.vpn.FocusLockVpnService

/**
 * Revives FocusLockVpnService automatically in the background if MyOS 13 / Android 13
 * stopped the task under memory load while protection should remain active.
 */
class ServiceWatchdogReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = FocusLockPreferences(context)
        if (!prefs.shouldMaintainVpn) return
        if (VpnService.prepare(context) != null) return

        val state = FocusLockVpnService.runtimeSnapshot.value.connectionState
        if (state != VpnConnectionState.ACTIVE) {
            runCatching {
                FocusLockVpnService.startOrSync(context)
            }.onFailure {
                FocusLockVpnService.postBootResumeNotification(context)
            }
        }
    }
}
