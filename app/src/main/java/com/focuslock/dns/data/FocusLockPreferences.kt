package com.focuslock.dns.data

import android.content.Context
import android.content.SharedPreferences
import com.focuslock.dns.model.DnsProfile
import com.focuslock.dns.model.LockTimerState

/**
 * Lightweight persistent state store.
 * Stores ONLY timer timestamps, active mode flag, streak/counter integers, commitment note,
 * language preference, and salted PIN hash.
 * NEVER stores DNS queries, visited domains, or browsing history.
 */
class FocusLockPreferences(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var shouldMaintainVpn: Boolean
        get() = prefs.getBoolean(KEY_SHOULD_MAINTAIN_VPN, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOULD_MAINTAIN_VPN, value).apply()

    var selectedPresetDays: Int
        get() = prefs.getInt(KEY_SELECTED_PRESET_DAYS, 7)
        set(value) = prefs.edit().putInt(KEY_SELECTED_PRESET_DAYS, value).apply()

    var customDays: Int
        get() = prefs.getInt(KEY_CUSTOM_DAYS, 0)
        set(value) = prefs.edit().putInt(KEY_CUSTOM_DAYS, value).apply()

    var customHours: Int
        get() = prefs.getInt(KEY_CUSTOM_HOURS, 12)
        set(value) = prefs.edit().putInt(KEY_CUSTOM_HOURS, value).apply()

    var isCustomDurationSelected: Boolean
        get() = prefs.getBoolean(KEY_IS_CUSTOM_DURATION, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_CUSTOM_DURATION, value).apply()

    var appLanguage: String
        get() = prefs.getString(KEY_APP_LANGUAGE, LANG_EN) ?: LANG_EN
        set(value) = prefs.edit().putString(KEY_APP_LANGUAGE, value).apply()

    var motivationalReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_MOTIVATIONAL_REMINDER, true)
        set(value) = prefs.edit().putBoolean(KEY_MOTIVATIONAL_REMINDER, value).apply()

    var cleanStreakStartEpochMs: Long
        get() = prefs.getLong(KEY_CLEAN_STREAK_START_MS, 0L)
        set(value) = prefs.edit().putLong(KEY_CLEAN_STREAK_START_MS, value).apply()

    var urgesResistedCount: Int
        get() = prefs.getInt(KEY_URGES_RESISTED_COUNT, 0)
        private set(value) = prefs.edit().putInt(KEY_URGES_RESISTED_COUNT, value).apply()

    var personalCommitmentNote: String
        get() = prefs.getString(KEY_PERSONAL_COMMITMENT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PERSONAL_COMMITMENT, value.take(160)).apply()

    var pinHashBase64: String?
        get() = prefs.getString(KEY_PIN_HASH, null)
        set(value) = prefs.edit().putString(KEY_PIN_HASH, value).apply()

    var pinSaltBase64: String?
        get() = prefs.getString(KEY_PIN_SALT, null)
        set(value) = prefs.edit().putString(KEY_PIN_SALT, value).apply()

    val isPinConfigured: Boolean
        get() = !pinHashBase64.isNullOrBlank() && !pinSaltBase64.isNullOrBlank()

    fun incrementUrgesResisted() {
        val next = urgesResistedCount + 1
        urgesResistedCount = next
    }

    fun ensureCleanStreakStarted(nowEpochMs: Long = System.currentTimeMillis()) {
        if (cleanStreakStartEpochMs <= 0L) {
            cleanStreakStartEpochMs = nowEpochMs
        }
    }

    fun resetCleanStreak(nowEpochMs: Long = System.currentTimeMillis()) {
        cleanStreakStartEpochMs = nowEpochMs
    }

    fun saveActiveTimer(startedAtEpochMs: Long, expiresAtEpochMs: Long, durationMs: Long) {
        if (cleanStreakStartEpochMs <= 0L) {
            cleanStreakStartEpochMs = startedAtEpochMs
        }
        prefs.edit()
            .putLong(KEY_LOCK_STARTED_AT, startedAtEpochMs)
            .putLong(KEY_LOCK_EXPIRES_AT, expiresAtEpochMs)
            .putLong(KEY_LOCK_DURATION_MS, durationMs)
            .putLong(KEY_CLEAN_STREAK_START_MS, cleanStreakStartEpochMs)
            .putBoolean(KEY_SHOULD_MAINTAIN_VPN, true)
            .commit()
    }

    fun clearExpiredTimer() {
        prefs.edit()
            .putLong(KEY_LOCK_STARTED_AT, 0L)
            .putLong(KEY_LOCK_EXPIRES_AT, 0L)
            .putLong(KEY_LOCK_DURATION_MS, 0L)
            .commit()
    }

    fun readTimerState(nowEpochMs: Long = System.currentTimeMillis()): LockTimerState {
        val startedAt = prefs.getLong(KEY_LOCK_STARTED_AT, 0L)
        val expiresAt = prefs.getLong(KEY_LOCK_EXPIRES_AT, 0L)
        val duration = prefs.getLong(KEY_LOCK_DURATION_MS, 0L)

        if (expiresAt <= 0L || nowEpochMs >= expiresAt) {
            return LockTimerState(isFocusLockActive = false)
        }

        return LockTimerState(
            isFocusLockActive = true,
            startedAtEpochMs = startedAt,
            expiresAtEpochMs = expiresAt,
            durationMs = duration.coerceAtLeast(expiresAt - startedAt),
            remainingMs = (expiresAt - nowEpochMs).coerceAtLeast(0L)
        )
    }

    fun resolveDesiredProfile(nowEpochMs: Long = System.currentTimeMillis()): DnsProfile {
        return if (readTimerState(nowEpochMs).isFocusLockActive) {
            DnsProfile.FOCUS_LOCK
        } else {
            DnsProfile.NORMAL
        }
    }

    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .apply()
    }

    companion object {
        const val LANG_EN = "en"
        const val LANG_BN = "bn"

        private const val PREFS_NAME = "focuslock_state_prefs"
        private const val KEY_SHOULD_MAINTAIN_VPN = "should_maintain_vpn"
        private const val KEY_LOCK_STARTED_AT = "lock_started_at_ms"
        private const val KEY_LOCK_EXPIRES_AT = "lock_expires_at_ms"
        private const val KEY_LOCK_DURATION_MS = "lock_duration_ms"
        private const val KEY_SELECTED_PRESET_DAYS = "selected_preset_days"
        private const val KEY_CUSTOM_DAYS = "custom_days"
        private const val KEY_CUSTOM_HOURS = "custom_hours"
        private const val KEY_IS_CUSTOM_DURATION = "is_custom_duration"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_MOTIVATIONAL_REMINDER = "motivational_reminder_enabled"
        private const val KEY_CLEAN_STREAK_START_MS = "clean_streak_start_ms"
        private const val KEY_URGES_RESISTED_COUNT = "urges_resisted_count"
        private const val KEY_PERSONAL_COMMITMENT = "personal_commitment_note"
        private const val KEY_PIN_HASH = "admin_pin_hash"
        private const val KEY_PIN_SALT = "admin_pin_salt"
    }
}
