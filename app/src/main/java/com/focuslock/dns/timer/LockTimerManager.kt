package com.focuslock.dns.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.focuslock.dns.data.FocusLockPreferences
import com.focuslock.dns.model.LockTimerState
import com.focuslock.dns.receiver.LockExpiryReceiver
import java.util.concurrent.TimeUnit

class LockTimerManager(
    private val context: Context,
    private val preferences: FocusLockPreferences
) {
    fun startFocusLockTimer(durationMs: Long): LockTimerState {
        val safeDuration = durationMs.coerceAtLeast(MIN_DURATION_MS)
        val now = System.currentTimeMillis()
        val expiresAt = now + safeDuration

        preferences.saveActiveTimer(
            startedAtEpochMs = now,
            expiresAtEpochMs = expiresAt,
            durationMs = safeDuration
        )

        scheduleExpiryCallbacks(expiresAt, safeDuration)
        return preferences.readTimerState(now)
    }

    fun restoreScheduleIfNeeded() {
        val state = preferences.readTimerState()
        if (state.isFocusLockActive) {
            scheduleExpiryCallbacks(state.expiresAtEpochMs, state.remainingMs)
        } else {
            preferences.clearExpiredTimer()
        }
    }

    private fun scheduleExpiryCallbacks(expiresAtEpochMs: Long, delayMs: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, LockExpiryReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (alarmManager != null) {
            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    expiresAtEpochMs,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    expiresAtEpochMs,
                    pendingIntent
                )
            }
        }

        // WorkManager backup guarantees Normal Mode transition even if MyOS clears alarms
        val workManager = WorkManager.getInstance(context)
        val oneTimeWork = OneTimeWorkRequestBuilder<LockExpiryWorker>()
            .setInitialDelay(delayMs.coerceAtLeast(1_000L), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(
            WORK_EXACT_EXPIRY,
            ExistingWorkPolicy.REPLACE,
            oneTimeWork
        )

        val watchdogWork = PeriodicWorkRequestBuilder<LockExpiryWorker>(
            15,
            TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            WORK_PERIODIC_WATCHDOG,
            ExistingPeriodicWorkPolicy.KEEP,
            watchdogWork
        )
    }

    companion object {
        private const val ALARM_REQUEST_CODE = 7402
        private const val MIN_DURATION_MS = LockTimerState.MILLIS_PER_HOUR
        private const val WORK_EXACT_EXPIRY = "focuslock_exact_expiry"
        private const val WORK_PERIODIC_WATCHDOG = "focuslock_periodic_watchdog"
    }
}
