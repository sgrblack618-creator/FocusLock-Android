package com.focuslock.dns.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.focuslock.dns.MainActivity
import com.focuslock.dns.R
import com.focuslock.dns.data.FocusLockPreferences
import com.focuslock.dns.data.MotivationalReminders
import com.focuslock.dns.model.LockTimerState
import com.focuslock.dns.model.VpnConnectionState
import com.focuslock.dns.ui.BlockReminderActivity
import com.focuslock.dns.vpn.FocusLockVpnService

/**
 * Live Dark Home Screen Widget for FocusLock.
 * Displays:
 * - Accurate live VPN/Lock status (never claims Protected when VPN is stopped)
 * - Remaining Focus Lock time
 * - Clean Streak days
 * - Hourly rotating Islamic & motivational quote with manual cycle action
 * - Direct Reflection Shield & Dashboard shortcuts
 */
class FocusLockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(context))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_NEXT_QUOTE) {
            val prefs = FocusLockPreferences(context)
            prefs.widgetQuoteOffset = (prefs.widgetQuoteOffset + 1) % MotivationalReminders.quotes.size
            requestUpdate(context)
        }
    }

    companion object {
        const val ACTION_NEXT_QUOTE = "com.focuslock.dns.widget.ACTION_NEXT_QUOTE"

        fun requestUpdate(context: Context) {
            val appContext = context.applicationContext
            val manager = AppWidgetManager.getInstance(appContext) ?: return
            val component = ComponentName(appContext, FocusLockWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return
            val views = buildRemoteViews(appContext)
            manager.updateAppWidget(ids, views)
        }

        private fun buildRemoteViews(context: Context): RemoteViews {
            val prefs = FocusLockPreferences(context)
            val nowMs = System.currentTimeMillis()
            val timer = prefs.readTimerState(nowMs)
            val isBn = prefs.appLanguage == FocusLockPreferences.LANG_BN

            val vpnState = FocusLockVpnService.runtimeSnapshot.value.connectionState
            val isVpnActive = vpnState == VpnConnectionState.ACTIVE

            val streakStart = prefs.cleanStreakStartEpochMs
            val streakDays = if (streakStart > 0L && nowMs > streakStart) {
                (nowMs - streakStart) / LockTimerState.MILLIS_PER_DAY
            } else {
                0L
            }

            val quoteIndex = MotivationalReminders.hourlyQuoteIndex(
                nowEpochMs = nowMs,
                offset = prefs.widgetQuoteOffset
            )
            val quote = MotivationalReminders.quoteForIndex(quoteIndex)

            val views = RemoteViews(context.packageName, R.layout.widget_focus_lock)

            val adsOn = prefs.adsBlockerEnabled

            // True status line & color
            val (statusText, statusColor) = when {
                isVpnActive && timer.isFocusLockActive -> {
                    val label = if (adsOn) {
                        if (isBn) "লক সক্রিয় · Family DNS" else "LOCKED · Family DNS"
                    } else {
                        if (isBn) "লক সক্রিয় · Ads Off" else "LOCKED · Ads Off"
                    }
                    label to Color.parseColor("#10B981")
                }
                isVpnActive -> {
                    val label = if (adsOn) {
                        if (isBn) "সাধারণ · Ad Block" else "NORMAL · Ad Block"
                    } else {
                        if (isBn) "সাধারণ · Ads Off" else "NORMAL · Ads Off"
                    }
                    label to Color.parseColor("#38BDF8")
                }
                timer.isFocusLockActive -> {
                    (if (isBn) "ভিপিএন বন্ধ — চালু করুন" else "VPN OFF · RESUME") to Color.parseColor("#EF4444")
                }
                else -> {
                    (if (isBn) "সুরক্ষা বন্ধ" else "VPN OFF") to Color.parseColor("#EF4444")
                }
            }
            views.setTextViewText(R.id.widget_status_text, statusText)
            views.setTextColor(R.id.widget_status_text, statusColor)

            // Labels & Values
            views.setTextViewText(
                R.id.widget_timer_label,
                if (isBn) "বাকি সময়" else "TIME LEFT"
            )
            val timerValue = when {
                timer.isFocusLockActive -> {
                    if (isBn) {
                        "${timer.remainingDays}দিন ${timer.remainingHoursPart}ঘণ্টা"
                    } else {
                        "${timer.remainingDays}d ${timer.remainingHoursPart}h left"
                    }
                }
                isVpnActive && adsOn -> if (isBn) "বিজ্ঞাপন ব্লক" else "Ad Block On"
                isVpnActive -> if (isBn) "বিজ্ঞাপন চালু" else "Ads Allowed"
                else -> if (isBn) "চালু নেই" else "Not Active"
            }
            views.setTextViewText(R.id.widget_timer_value, timerValue)

            views.setTextViewText(
                R.id.widget_streak_label,
                if (isBn) "পবিত্র ধারা" else "CLEAN STREAK"
            )
            views.setTextViewText(
                R.id.widget_streak_value,
                if (isBn) "$streakDays দিন পবিত্র" else "$streakDays Days Clean"
            )

            // Hourly Quote
            if (!quote.arabicLine.isNullOrBlank()) {
                views.setViewVisibility(R.id.widget_quote_arabic, View.VISIBLE)
                views.setTextViewText(R.id.widget_quote_arabic, quote.arabicLine)
            } else {
                views.setViewVisibility(R.id.widget_quote_arabic, View.GONE)
            }
            views.setTextViewText(
                R.id.widget_quote_title,
                if (isBn) quote.titleBn else quote.titleEn
            )
            views.setTextViewText(
                R.id.widget_quote_source,
                if (isBn) quote.sourceBn else quote.sourceEn
            )

            // Bottom buttons
            views.setTextViewText(
                R.id.widget_btn_shield,
                if (isBn) "রিফ্লেকশন শিল্ড" else "Reflection Shield"
            )
            views.setTextViewText(
                R.id.widget_btn_open_app,
                if (isBn) "অ্যাপ খুলুন" else "Open App"
            )

            // PendingIntents
            val openAppIntent = PendingIntent.getActivity(
                context,
                101,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppIntent)
            views.setOnClickPendingIntent(R.id.widget_btn_open_app, openAppIntent)

            val shieldIntent = PendingIntent.getActivity(
                context,
                102,
                Intent(context, BlockReminderActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(BlockReminderActivity.EXTRA_QUOTE_INDEX, quoteIndex)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_shield, shieldIntent)

            val nextQuoteIntent = PendingIntent.getBroadcast(
                context,
                103,
                Intent(context, FocusLockWidgetProvider::class.java).apply {
                    action = ACTION_NEXT_QUOTE
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next_quote, nextQuoteIntent)

            return views
        }
    }
}
