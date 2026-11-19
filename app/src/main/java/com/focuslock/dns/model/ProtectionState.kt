package com.focuslock.dns.model

import com.focuslock.dns.data.FocusLockPreferences

enum class VpnConnectionState {
    DISCONNECTED,
    CONNECTING,
    ACTIVE,
    REVOKED,
    ERROR
}

data class VpnRuntimeSnapshot(
    val connectionState: VpnConnectionState = VpnConnectionState.DISCONNECTED,
    val activeProfile: DnsProfile = DnsProfile.NORMAL,
    val lastError: String? = null
)

data class LockTimerState(
    val isFocusLockActive: Boolean = false,
    val startedAtEpochMs: Long = 0L,
    val expiresAtEpochMs: Long = 0L,
    val durationMs: Long = 0L,
    val remainingMs: Long = 0L
) {
    val progressFraction: Float
        get() {
            if (!isFocusLockActive || durationMs <= 0L) return 0f
            return (remainingMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }

    val remainingDays: Long
        get() = (remainingMs / MILLIS_PER_DAY).coerceAtLeast(0L)

    val remainingHoursPart: Long
        get() = ((remainingMs % MILLIS_PER_DAY) / MILLIS_PER_HOUR).coerceAtLeast(0L)

    val remainingMinutesPart: Long
        get() = ((remainingMs % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE).coerceAtLeast(0L)

    val remainingSecondsPart: Long
        get() = ((remainingMs % MILLIS_PER_MINUTE) / 1000L).coerceAtLeast(0L)

    companion object {
        const val MILLIS_PER_MINUTE = 60_000L
        const val MILLIS_PER_HOUR = 3_600_000L
        const val MILLIS_PER_DAY = 86_400_000L
    }
}

data class DashboardUiState(
    val vpnSnapshot: VpnRuntimeSnapshot = VpnRuntimeSnapshot(),
    val timerState: LockTimerState = LockTimerState(),
    val selectedPresetDays: Int = 7,
    val customDays: Int = 0,
    val customHours: Int = 12,
    val isCustomDurationSelected: Boolean = false,
    val isPinConfigured: Boolean = false,
    val isPrivateDnsWarningVisible: Boolean = false,
    val hasNotificationPermission: Boolean = true,
    val appLanguage: String = FocusLockPreferences.LANG_EN,
    val motivationalReminderEnabled: Boolean = true,
    val canDrawOverlays: Boolean = false,
    val cleanStreakDays: Long = 0L,
    val urgesResistedCount: Int = 0,
    val personalCommitmentNote: String = "",
    val activeQuoteIndex: Int = 0,
    val excludeYouTubeApp: Boolean = false,
    val ruleBlockAdultSites: Boolean = true,
    val ruleBlockGamblingSites: Boolean = true,
    val ruleBlockSocialShorts: Boolean = false,
    val adsBlockerEnabled: Boolean = true,
    val isBatteryOptimizationIgnored: Boolean = false
) {
    val effectiveDurationMs: Long
        get() = if (isCustomDurationSelected) {
            (customDays * LockTimerState.MILLIS_PER_DAY) +
                (customHours * LockTimerState.MILLIS_PER_HOUR)
        } else {
            selectedPresetDays * LockTimerState.MILLIS_PER_DAY
        }

    val isProtectionActuallyRunning: Boolean
        get() = vpnSnapshot.connectionState == VpnConnectionState.ACTIVE
}
