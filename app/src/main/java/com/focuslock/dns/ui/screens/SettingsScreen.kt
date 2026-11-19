package com.focuslock.dns.ui.screens

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

@Composable
fun SettingsScreen(
    state: DashboardUiState,
    strings: AppStrings,
    onSetPin: (String) -> Boolean,
    onVerifyPin: (String) -> Boolean,
    onRemovePin: (String) -> Boolean,
    onSetMotivationalReminder: (Boolean) -> Unit,
    onSaveCommitment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showSetPinDialog by remember { mutableStateOf(false) }
    var showVerifyToChangePin by remember { mutableStateOf(false) }
    var showVerifyToRemovePin by remember { mutableStateOf(false) }
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

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
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
                            onClick = { showVerifyToRemovePin = true },
                            border = BorderStroke(1.dp, FocusLockColors.DangerRed.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(strings.pinRemoveBtn, color = FocusLockColors.DangerRed, fontSize = 13.sp)
                        }
                    }
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
                    text = "${strings.profileNormalDesc} (${DnsProfile.NORMAL.dohUrl})",
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
                    text = "${strings.profileFocusDesc} (${DnsProfile.FOCUS_LOCK.dohUrl})",
                    fontSize = 12.sp,
                    color = FocusLockColors.TextSecondary
                )
            }
        }

        // ZTE Blade A54 / MyOS 13 Battery Optimization Helper
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
                    text = strings.batteryDesc,
                    fontSize = 12.sp,
                    color = FocusLockColors.TextSecondary
                )
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            )
                        }
                    },
                    border = BorderStroke(1.dp, FocusLockColors.Hairline),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.batteryBtn, color = FocusLockColors.TextPrimary, fontSize = 13.sp)
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
}
