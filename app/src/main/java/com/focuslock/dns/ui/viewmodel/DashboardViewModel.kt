package com.focuslock.dns.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.dns.data.FocusLockPreferences
import com.focuslock.dns.data.MotivationalReminders
import com.focuslock.dns.model.DashboardUiState
import com.focuslock.dns.model.LockTimerState
import com.focuslock.dns.model.VpnConnectionState
import com.focuslock.dns.security.PinSecurityManager
import com.focuslock.dns.timer.LockTimerManager
import com.focuslock.dns.vpn.FocusLockVpnService
import com.focuslock.dns.widget.FocusLockWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = FocusLockPreferences(application)
    private val pinManager = PinSecurityManager(preferences)
    private val timerManager = LockTimerManager(application, preferences)

    private val configState = MutableStateFlow(readConfigSnapshot())
    private val quoteIndexFlow = MutableStateFlow(0)
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000L)
        }
    }

    init {
        timerManager.restoreScheduleIfNeeded()

        viewModelScope.launch {
            combine(
                FocusLockVpnService.runtimeSnapshot,
                tickerFlow,
                configState,
                quoteIndexFlow
            ) { vpnSnapshot, nowMs, config, quoteIndex ->
                val timer = preferences.readTimerState(nowMs)
                val wasExpiredNow = !timer.isFocusLockActive &&
                    vpnSnapshot.activeProfile.isFocusLockAdultFilter

                if (wasExpiredNow) {
                    preferences.clearExpiredTimer()
                    if (preferences.shouldMaintainVpn) {
                        FocusLockVpnService.syncProfile(getApplication())
                    }
                }

                val streakStart = preferences.cleanStreakStartEpochMs
                val streakDays = if (streakStart > 0L && nowMs > streakStart) {
                    (nowMs - streakStart) / LockTimerState.MILLIS_PER_DAY
                } else {
                    0L
                }

                DashboardUiState(
                    vpnSnapshot = vpnSnapshot,
                    timerState = timer,
                    selectedPresetDays = config.selectedPresetDays,
                    customDays = config.customDays,
                    customHours = config.customHours,
                    isCustomDurationSelected = config.isCustomDurationSelected,
                    isPinConfigured = config.isPinConfigured,
                    isPrivateDnsWarningVisible = config.isPrivateDnsWarningVisible,
                    hasNotificationPermission = config.hasNotificationPermission,
                    appLanguage = config.appLanguage,
                    motivationalReminderEnabled = config.motivationalReminderEnabled,
                    canDrawOverlays = config.canDrawOverlays,
                    cleanStreakDays = streakDays,
                    urgesResistedCount = preferences.urgesResistedCount,
                    personalCommitmentNote = config.personalCommitmentNote,
                    activeQuoteIndex = quoteIndex,
                    excludeYouTubeApp = config.excludeYouTubeApp,
                    ruleBlockAdultSites = config.ruleBlockAdultSites,
                    ruleBlockGamblingSites = config.ruleBlockGamblingSites,
                    ruleBlockSocialShorts = config.ruleBlockSocialShorts,
                    adsBlockerEnabled = config.adsBlockerEnabled,
                    isBatteryOptimizationIgnored = config.isBatteryOptimizationIgnored
                )
            }.collect { updated ->
                _uiState.value = updated
            }
        }
    }

    fun refreshSystemChecks(hasNotificationPermission: Boolean) {
        configState.value = readConfigSnapshot(hasNotificationPermission)
    }

    fun toggleLanguage() {
        val next = if (preferences.appLanguage == FocusLockPreferences.LANG_EN) {
            FocusLockPreferences.LANG_BN
        } else {
            FocusLockPreferences.LANG_EN
        }
        preferences.appLanguage = next
        configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
        FocusLockWidgetProvider.requestUpdate(getApplication())
    }

    fun nextQuote() {
        quoteIndexFlow.value = (quoteIndexFlow.value + 1) % MotivationalReminders.quotes.size
    }

    fun setMotivationalReminderEnabled(enabled: Boolean) {
        if (preferences.readTimerState().isFocusLockActive) return
        preferences.motivationalReminderEnabled = enabled
        configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
    }

    fun setExcludeYouTubeApp(enabled: Boolean) {
        if (preferences.readTimerState().isFocusLockActive) return
        preferences.excludeYouTubeApp = enabled
        configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
        if (preferences.shouldMaintainVpn ||
            FocusLockVpnService.runtimeSnapshot.value.connectionState == VpnConnectionState.ACTIVE
        ) {
            FocusLockVpnService.syncProfile(getApplication())
        }
    }

    fun setRuleBlockAdultSites(enabled: Boolean) {
        if (preferences.readTimerState().isFocusLockActive) return
        preferences.ruleBlockAdultSites = enabled
        configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
    }

    fun setRuleBlockGamblingSites(enabled: Boolean) {
        if (preferences.readTimerState().isFocusLockActive) return
        preferences.ruleBlockGamblingSites = enabled
        configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
    }

    fun setRuleBlockSocialShorts(enabled: Boolean) {
        if (preferences.readTimerState().isFocusLockActive) return
        preferences.ruleBlockSocialShorts = enabled
        configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
    }

    /**
     * Intentionally remains switchable ON/OFF even while Focus Lock & PIN are locked,
     * so sites/apps that block ad-blockers remain usable without weakening Adult/Gambling/Social locks.
     */
    fun setAdsBlockerEnabled(enabled: Boolean) {
        preferences.adsBlockerEnabled = enabled
        configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
        if (preferences.shouldMaintainVpn ||
            FocusLockVpnService.runtimeSnapshot.value.connectionState == VpnConnectionState.ACTIVE
        ) {
            FocusLockVpnService.syncProfile(getApplication())
        }
        FocusLockWidgetProvider.requestUpdate(getApplication())
    }

    fun savePersonalCommitment(note: String) {
        preferences.personalCommitmentNote = note.trim()
        configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
    }

    fun selectPresetDays(days: Int) {
        preferences.selectedPresetDays = days
        preferences.isCustomDurationSelected = false
        configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
    }

    fun selectCustomDuration(days: Int, hours: Int) {
        val safeDays = days.coerceIn(0, 365)
        val safeHours = hours.coerceIn(0, 23)
        val normalizedHours = if (safeDays == 0 && safeHours == 0) 1 else safeHours

        preferences.customDays = safeDays
        preferences.customHours = normalizedHours
        preferences.isCustomDurationSelected = true
        configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
    }

    fun activateNormalModeVpn() {
        preferences.shouldMaintainVpn = true
        preferences.ensureCleanStreakStarted()
        FocusLockVpnService.startOrSync(getApplication())
    }

    fun startFocusLockProtection() {
        val durationMs = _uiState.value.effectiveDurationMs
            .coerceAtLeast(LockTimerState.MILLIS_PER_HOUR)
        timerManager.startFocusLockTimer(durationMs)
        FocusLockVpnService.startOrSync(getApplication())
        configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
    }

    fun verifyPin(pin: String): Boolean = pinManager.verifyPin(pin)

    fun setPin(newPin: String): Boolean {
        val saved = pinManager.setPin(newPin)
        if (saved) {
            configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
        }
        return saved
    }

    fun removePin(currentPin: String): Boolean {
        val removed = pinManager.removePin(currentPin)
        if (removed) {
            configState.value = readConfigSnapshot(configState.value.hasNotificationPermission)
        }
        return removed
    }

    private fun readConfigSnapshot(hasNotificationPermission: Boolean = true): ConfigSnapshot {
        val app = getApplication<Application>()
        val powerManager = app.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val batteryExempt = powerManager?.isIgnoringBatteryOptimizations(app.packageName) == true
        return ConfigSnapshot(
            selectedPresetDays = preferences.selectedPresetDays,
            customDays = preferences.customDays,
            customHours = preferences.customHours,
            isCustomDurationSelected = preferences.isCustomDurationSelected,
            isPinConfigured = pinManager.isPinConfigured(),
            isPrivateDnsWarningVisible = FocusLockVpnService.isSystemPrivateDnsStrict(app),
            hasNotificationPermission = hasNotificationPermission,
            appLanguage = preferences.appLanguage,
            motivationalReminderEnabled = preferences.motivationalReminderEnabled,
            canDrawOverlays = Settings.canDrawOverlays(app),
            personalCommitmentNote = preferences.personalCommitmentNote,
            excludeYouTubeApp = preferences.excludeYouTubeApp,
            ruleBlockAdultSites = preferences.ruleBlockAdultSites,
            ruleBlockGamblingSites = preferences.ruleBlockGamblingSites,
            ruleBlockSocialShorts = preferences.ruleBlockSocialShorts,
            adsBlockerEnabled = preferences.adsBlockerEnabled,
            isBatteryOptimizationIgnored = batteryExempt
        )
    }

    private data class ConfigSnapshot(
        val selectedPresetDays: Int,
        val customDays: Int,
        val customHours: Int,
        val isCustomDurationSelected: Boolean,
        val isPinConfigured: Boolean,
        val isPrivateDnsWarningVisible: Boolean,
        val hasNotificationPermission: Boolean,
        val appLanguage: String,
        val motivationalReminderEnabled: Boolean,
        val canDrawOverlays: Boolean,
        val personalCommitmentNote: String,
        val excludeYouTubeApp: Boolean,
        val ruleBlockAdultSites: Boolean,
        val ruleBlockGamblingSites: Boolean,
        val ruleBlockSocialShorts: Boolean,
        val adsBlockerEnabled: Boolean,
        val isBatteryOptimizationIgnored: Boolean
    )
}
