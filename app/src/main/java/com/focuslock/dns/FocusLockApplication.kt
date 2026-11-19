package com.focuslock.dns

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class FocusLockApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java) ?: return

            val vpnChannel = NotificationChannel(
                CHANNEL_VPN_STATUS,
                getString(R.string.notification_channel_vpn),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_vpn_desc)
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                CHANNEL_PROTECTION_ALERTS,
                getString(R.string.notification_channel_alert),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_alert_desc)
            }

            manager.createNotificationChannel(vpnChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val CHANNEL_VPN_STATUS = "focuslock_vpn_status"
        const val CHANNEL_PROTECTION_ALERTS = "focuslock_alerts"
    }
}
