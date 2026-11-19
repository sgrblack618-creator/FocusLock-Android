package com.focuslock.dns.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.dns.model.DashboardUiState
import com.focuslock.dns.model.DnsProfile
import com.focuslock.dns.ui.components.SetAdminPinDialog
import com.focuslock.dns.ui.components.VerifyAdminPinDialog
import com.focuslock.dns.ui.strings.AppStrings
import com.focuslock.dns.ui.theme.FocusLockColors
import com.focuslock.dns.widget.FocusLockWidgetProvider

@Composable
fun SettingsScreen(
    state: DashboardUiState,
    strings: AppStrings,
    onSetPin: (String) -> Boolean,
    onVerifyPin: (String) -> Boolean,
    onRemovePin: (String) -> Boolean,
    onSetMotivationalReminder: (Boolean) -> Unit,
    onSetExcludeYouTube: (Boolean) -> Unit,
    onSetAdsBlockerEnabled: (Boolean) -> Unit,
    onSaveCommitment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showSetPinDialog by remember { mutableStateOf(false) }
    var showVerifyToChangePin by remember { mutableStateOf(false) }
    var showVerifyToRemovePin by remember { mutableStateOf(false) }
    var pendingYouTubeToggle by remember { mutableStateOf<Boolean?>(null) }
    var commitmentDraft by remember(state.personalCommitmentNote) {
        mutableStateOf(state.personalCommitmentNote)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FocusLockColors.Canvas)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.settingsTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = FocusLockColors.TextPrimary
        )

        // Personal Commitment ("My Why") Editor Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.commitmentHeader,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.TextPrimary
                )
                OutlinedTextField(
                    value = commitmentDraft,
                    onValueChange = { if (it.length <= 160) commitmentDraft = it },
                    placeholder = { Text(strings.commitmentPlaceholder, fontSize = 12.sp) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onSaveCommitment(commitmentDraft) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FocusLockColors.FocusEmerald,
                        contentColor = FocusLockColors.Canvas
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.saveCommitmentBtn, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Islamic / Motivational Block Reminder Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.reminderSectionTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.TextPrimary
                )
                Text(
                    text = strings.reminderSectionDesc,
                    fontSize = 12.sp,
                    color = FocusLockColors.TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.reminderToggleLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = FocusLockColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.motivationalReminderEnabled,
                        enabled = !state.timerState.isFocusLockActive,
                        onCheckedChange = onSetMotivationalReminder,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FocusLockColors.Canvas,
                            checkedTrackColor = FocusLockColors.FocusEmerald
                        )
                    )
                }

                Text(
                    text = if (state.canDrawOverlays) strings.overlayGrantedText else strings.overlayMissingText,
                    fontSize = 12.sp,
                    color = if (state.canDrawOverlays) FocusLockColors.FocusEmerald else FocusLockColors.TextMuted
                )

                if (!state.canDrawOverlays) {
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                val overlayIntent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(overlayIntent)
                            }
                        },
                        border = BorderStroke(1.dp, FocusLockColors.FocusEmerald.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.overlayGrantBtn, color = FocusLockColors.FocusEmerald, fontSize = 12.sp)
                    }
                }
            }
        }

        // Always-Switchable Ads Blocker Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${strings.adsBlockerCardTitle} (${strings.adsBlockerAlwaysSwitchableBadge})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.TextPrimary
                )
                Text(
                    text = if (state.adsBlockerEnabled) strings.adsBlockerOnDesc else strings.adsBlockerOffDesc,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = FocusLockColors.TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.adsBlockerSwitchLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = FocusLockColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.adsBlockerEnabled,
                        enabled = true,
                        onCheckedChange = onSetAdsBlockerEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FocusLockColors.Canvas,
                            checkedTrackColor = FocusLockColors.NormalSky
                        )
                    )
                }
            }
        }

        // YouTube Comments & App Exclusion Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.youtubeSectionTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.TextPrimary
                )
                Text(
                    text = strings.youtubeSectionDesc,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = FocusLockColors.TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.youtubeToggleLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = FocusLockColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.excludeYouTubeApp,
                        enabled = !state.timerState.isFocusLockActive,
                        onCheckedChange = { nextValue ->
                            if (state.isPinConfigured) {
                                pendingYouTubeToggle = nextValue
                            } else {
                                onSetExcludeYouTube(nextValue)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FocusLockColors.Canvas,
                            checkedTrackColor = FocusLockColors.FocusEmerald
                        )
                    )
                }
                Text(
                    text = strings.youtubeRestartHint,
                    fontSize = 12.sp,
                    color = FocusLockColors.NormalSky
                )
                if (state.isPinConfigured) {
                    Text(
                        text = strings.youtubePinNote,
                        fontSize = 11.sp,
                        color = FocusLockColors.WarningAmber
                    )
                }
            }
        }

        // Home Screen Live Dark Widget Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.widgetSectionTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.TextPrimary
                )
                Text(
                    text = strings.widgetSectionDesc,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = FocusLockColors.TextSecondary
                )
                Button(
                    onClick = {
                        runCatching {
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            val provider = ComponentName(context, FocusLockWidgetProvider::class.java)
                            if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                                appWidgetManager.requestPinAppWidget(provider, null, null)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FocusLockColors.FocusEmerald,
                        contentColor = FocusLockColors.Canvas
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.widgetPinBtn, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Text(
                    text = strings.widgetManualHint,
                    fontSize = 11.sp,
                    color = FocusLockColors.TextMuted
                )
            }
        }

        // Admin PIN Protection Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.pinTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.TextPrimary
                )
                Text(
                    text = if (state.isPinConfigured) strings.pinActiveDesc else strings.pinUnsetDesc,
                    fontSize = 13.sp,
                    color = FocusLockColors.TextSecondary
                )

                val pinEditLocked = state.isPinConfigured && state.timerState.isFocusLockActive
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        enabled = !pinEditLocked,
                        onClick = {
                            if (state.isPinConfigured) {
                                showVerifyToChangePin = true
                            } else {
                                showSetPinDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FocusLockColors.FocusEmerald,
                            contentColor = FocusLockColors.Canvas
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (state.isPinConfigured) strings.pinChangeBtn else strings.pinSetBtn,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    if (state.isPinConfigured) {
                        OutlinedButton(
                            enabled = !pinEditLocked,
                            onClick = { showVerifyToRemovePin = true },
                            border = BorderStroke(
                                1.dp,
                                if (pinEditLocked) FocusLockColors.Hairline else FocusLockColors.DangerRed.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = strings.pinRemoveBtn,
                                color = if (pinEditLocked) FocusLockColors.TextMuted else FocusLockColors.DangerRed,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (pinEditLocked) {
                    Text(
                        text = strings.pinLockedDuringTimerNote,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = FocusLockColors.FocusEmerald
                    )
                }

                Text(
                    text = strings.pinLimitationNote,
                    fontSize = 12.sp,
                    color = FocusLockColors.WarningAmber
                )
            }
        }

        // Dual DNS Profiles Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.dualProfileSectionTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.TextPrimary
                )

                Text(
                    text = "1. ${strings.profileNormalTitle}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.NormalSky
                )
                Text(
                    text = "${strings.profileNormalDesc} (${DnsProfile.NORMAL.dohUrl})\n" +
                        "Ads OFF: ${DnsProfile.NORMAL_NO_ADBLOCK.dohUrl}",
                    fontSize = 12.sp,
                    color = FocusLockColors.TextSecondary
                )

                HorizontalDivider(color = FocusLockColors.Hairline)

                Text(
                    text = "2. ${strings.profileFocusTitle}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.FocusEmerald
                )
                Text(
                    text = "${strings.profileFocusDesc} (${DnsProfile.FOCUS_LOCK.dohUrl})\n" +
                        "Ads OFF: ${DnsProfile.FOCUS_LOCK_NO_ADBLOCK.dohUrl}",
                    fontSize = 12.sp,
                    color = FocusLockColors.TextSecondary
                )
            }
        }

        // ZTE Blade A54 / MyOS 13 Battery Optimization & Always-on VPN Helper
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.batteryTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.TextPrimary
                )
                Text(
                    text = if (state.isBatteryOptimizationIgnored) strings.antiKillOkText else strings.batteryDesc,
                    fontSize = 12.sp,
                    color = if (state.isBatteryOptimizationIgnored) FocusLockColors.FocusEmerald else FocusLockColors.TextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                val intent = if (!state.isBatteryOptimizationIgnored) {
                                    Intent(
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                } else {
                                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                }
                                context.startActivity(intent)
                            }.onFailure {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                }
                            }
                        },
                        border = BorderStroke(1.dp, FocusLockColors.Hairline),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(strings.batteryBtn, color = FocusLockColors.TextPrimary, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
                            }
                        },
                        border = BorderStroke(1.dp, FocusLockColors.FocusEmerald.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(strings.antiKillVpnAlwaysOnBtn, color = FocusLockColors.FocusEmerald, fontSize = 12.sp)
                    }
                }
            }
        }

        // Privacy & Technical Disclosures
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.privacyTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.TextPrimary
                )
                Text(
                    text = strings.privacyBullets,
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    color = FocusLockColors.TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showSetPinDialog) {
        SetAdminPinDialog(
            strings = strings,
            onSavePin = onSetPin,
            onDismiss = { showSetPinDialog = false }
        )
    }

    if (showVerifyToChangePin) {
        VerifyAdminPinDialog(
            strings = strings,
            onVerify = onVerifyPin,
            onDismiss = { showVerifyToChangePin = false },
            onSuccess = {
                showVerifyToChangePin = false
                showSetPinDialog = true
            }
        )
    }

    if (showVerifyToRemovePin) {
        VerifyAdminPinDialog(
            strings = strings,
            onVerify = onVerifyPin,
            onDismiss = { showVerifyToRemovePin = false },
            onSuccess = { pin ->
                onRemovePin(pin)
                showVerifyToRemovePin = false
            }
        )
    }

    pendingYouTubeToggle?.let { targetValue ->
        VerifyAdminPinDialog(
            strings = strings,
            onVerify = onVerifyPin,
            onDismiss = { pendingYouTubeToggle = null },
            onSuccess = {
                onSetExcludeYouTube(targetValue)
                pendingYouTubeToggle = null
            }
        )
    }
}
