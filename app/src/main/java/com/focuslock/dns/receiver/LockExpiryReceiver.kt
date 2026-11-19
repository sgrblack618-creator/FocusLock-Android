package com.focuslock.dns.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focuslock.dns.data.FocusLockPreferences
import com.focuslock.dns.vpn.FocusLockVpnService
import com.focuslock.dns.widget.FocusLockWidgetProvider

class LockExpiryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = FocusLockPreferences(context)
        val timer = prefs.readTimerState()
        if (!timer.isFocusLockActive) {
            prefs.clearExpiredTimer()
            if (prefs.shouldMaintainVpn) {
                FocusLockVpnService.syncProfile(context)
            }
        }
        FocusLockWidgetProvider.requestUpdate(context)
    }
}
