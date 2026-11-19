package com.focuslock.dns.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuslock.dns.data.FocusLockPreferences
import com.focuslock.dns.data.MotivationalReminders
import com.focuslock.dns.model.DashboardUiState
import com.focuslock.dns.model.DnsProfile
import com.focuslock.dns.model.VpnConnectionState
import com.focuslock.dns.ui.BlockReminderActivity
import com.focuslock.dns.ui.components.CircularCountdownRing
import com.focuslock.dns.ui.components.CustomDurationDialog
import com.focuslock.dns.ui.components.VerifyAdminPinDialog
import com.focuslock.dns.ui.strings.AppStrings
import com.focuslock.dns.ui.theme.FocusLockColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    strings: AppStrings,
    onToggleLanguage: () -> Unit,
    onNextQuote: () -> Unit,
    onSelectPresetDays: (Int) -> Unit,
    onSelectCustomDuration: (Int, Int) -> Unit,
    onRequestStartFocusLock: () -> Unit,
    onRequestEnableNormalVpn: () -> Unit,
    onSetRuleAdult: (Boolean) -> Unit,
    onSetRuleGambling: (Boolean) -> Unit,
    onSetRuleSocial: (Boolean) -> Unit,
    onSetExcludeYouTube: (Boolean) -> Unit,
    onSetMotivationalReminder: (Boolean) -> Unit,
    onSetAdsBlockerEnabled: (Boolean) -> Unit,
    onVerifyPin: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isBn = state.appLanguage == FocusLockPreferences.LANG_BN

    var showCustomDialog by remember { mutableStateOf(false) }
    var pendingPinAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val selectedDurationLabel = if (state.isCustomDurationSelected) {
        buildString {
            if (state.customDays > 0) append("${state.customDays} ${strings.daysUnit} ")
            if (state.customHours > 0) append("${state.customHours} ${strings.hoursUnit}")
        }.trim()
    } else {
        "${state.selectedPresetDays} ${strings.daysUnit}"
    }

    val runWithOptionalPin: (() -> Unit) -> Unit = { action ->
        if (state.isPinConfigured) {
            pendingPinAction = action
        } else {
            action()
        }
    }

    val activeQuote = MotivationalReminders.quoteForIndex(state.activeQuoteIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FocusLockColors.Canvas)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // FocusLock Brand Header + Language Switch + True Protection Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FocusLockColors.FocusEmeraldSoft)
                        .border(1.dp, FocusLockColors.FocusEmerald.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = "FocusLock",
                        tint = FocusLockColors.FocusEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FocusLock",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusLockColors.TextPrimary
                    )
                    Text(
                        text = strings.brandSubtitle,
                        fontSize = 12.sp,
                        color = FocusLockColors.TextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val statusColor = when {
                    state.isProtectionActuallyRunning && state.timerState.isFocusLockActive ->
                        FocusLockColors.FocusEmerald
                    state.isProtectionActuallyRunning ->
                        FocusLockColors.NormalSky
                    else -> FocusLockColors.DangerRed
                }
                val statusLabel = when {
                    state.isProtectionActuallyRunning && state.timerState.isFocusLockActive ->
                        strings.statusProtectedLocked
                    state.isProtectionActuallyRunning ->
                        strings.statusProtectedNormal
                    state.vpnSnapshot.connectionState == VpnConnectionState.CONNECTING ->
                        strings.statusConnecting
                    else -> strings.statusStopped
                }
                Text(
                    text = statusLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FocusLockColors.SurfaceElevated)
                        .border(1.dp, FocusLockColors.Hairline, RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleLanguage)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = strings.switchLangLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = FocusLockColors.TextPrimary
                    )
                }
            }
        }

        // Android System Private DNS warning
        if (state.isPrivateDnsWarningVisible) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FocusLockColors.WarningAmberSoft,
                border = BorderStroke(1.dp, FocusLockColors.WarningAmber.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = FocusLockColors.WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.privateDnsTitle,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = FocusLockColors.TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.privateDnsBody,
                        fontSize = 12.sp,
                        color = FocusLockColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                            }
                        },
                        border = BorderStroke(1.dp, FocusLockColors.WarningAmber)
                    ) {
                        Text(strings.openNetworkSettings, color = FocusLockColors.WarningAmber, fontSize = 12.sp)
                    }
                }
            }
        }

        // ZTE Blade A54 / MyOS 13 Anti-Kill Background Stability Banner
        if (!state.isBatteryOptimizationIgnored) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FocusLockColors.SurfaceElevated,
                border = BorderStroke(1.dp, FocusLockColors.NormalSky.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.antiKillTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = FocusLockColors.NormalSky
                    )
                    Text(
                        text = strings.antiKillBody,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = FocusLockColors.TextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                    )
                                }.onFailure {
                                    runCatching {
                                        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FocusLockColors.NormalSky,
                                contentColor = FocusLockColors.Canvas
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.antiKillBatteryBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
                                }
                            },
                            border = BorderStroke(1.dp, FocusLockColors.FocusEmerald.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.antiKillVpnAlwaysOnBtn, fontSize = 11.sp, color = FocusLockColors.FocusEmerald)
                        }
                    }
                }
            }
        }

        // Main Circular Countdown Hero Surface
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularCountdownRing(
                    vpnState = state.vpnSnapshot.connectionState,
                    timerState = state.timerState,
                    selectedDurationLabel = selectedDurationLabel,
                    strings = strings,
                    adsBlockerEnabled = state.adsBlockerEnabled
                )

                Spacer(modifier = Modifier.height(14.dp))

                val activeProfile = state.vpnSnapshot.activeProfile
                Text(
                    text = when {
                        !state.isProtectionActuallyRunning -> strings.vpnOffTitle
                        activeProfile.isFocusLockAdultFilter -> strings.profileFocusTitle
                        else -> strings.profileNormalTitle
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.isProtectionActuallyRunning) {
                        FocusLockColors.TextPrimary
                    } else {
                        FocusLockColors.DangerRed
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        !state.isProtectionActuallyRunning -> strings.vpnOffSubtitle
                        activeProfile == DnsProfile.FOCUS_LOCK -> strings.profileFocusDesc
                        activeProfile == DnsProfile.FOCUS_LOCK_NO_ADBLOCK -> strings.profileFocusNoAdDesc
                        activeProfile == DnsProfile.NORMAL -> strings.profileNormalDesc
                        else -> strings.profileNormalNoAdDesc
                    },
                    fontSize = 12.sp,
                    color = FocusLockColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    state.timerState.isFocusLockActive && !state.isProtectionActuallyRunning -> {
                        Button(
                            onClick = onRequestStartFocusLock,
                            colors = ButtonDefaults.buttonColors(containerColor = FocusLockColors.DangerRed),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = strings.resumeVpnCta,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    state.timerState.isFocusLockActive -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = FocusLockColors.FocusEmeraldSoft,
                            border = BorderStroke(1.dp, FocusLockColors.FocusEmerald.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = FocusLockColors.FocusEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.lockActiveBanner,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = FocusLockColors.TextPrimary
                                )
                            }
                        }
                    }

                    else -> {
                        Button(
                            onClick = { runWithOptionalPin(onRequestStartFocusLock) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FocusLockColors.FocusEmerald,
                                contentColor = FocusLockColors.Canvas
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = "${strings.startFocusLockCta} ($selectedDurationLabel)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (!state.isProtectionActuallyRunning) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onRequestEnableNormalVpn,
                                border = BorderStroke(1.dp, FocusLockColors.NormalSky.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = strings.enableNormalOnlyCta,
                                    color = FocusLockColors.NormalSky,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Clean Streak & Urges Resisted Discipline Strip (Pure integer stats, zero browsing history)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FocusLockColors.Surface,
                border = BorderStroke(1.dp, FocusLockColors.Hairline),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = strings.streakLabel, fontSize = 11.sp, color = FocusLockColors.TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.cleanStreakDays} ${strings.streakUnitDays}",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusLockColors.FocusEmerald,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FocusLockColors.Surface,
                border = BorderStroke(1.dp, FocusLockColors.Hairline),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = strings.urgesResistedLabel, fontSize = 11.sp, color = FocusLockColors.TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.urgesResistedCount.toString(),
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FocusLockColors.NormalSky,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            }
        }

        // Featured Motivational / Islamic Quote Card + Emergency Reflection Shield
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                activeQuote.imageResId?.let { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xD6090D12), Color(0xEB090D12))
                                )
                            )
                    )
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.quoteCardLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FocusLockColors.FocusEmerald
                        )
                        TextButton(onClick = onNextQuote) {
                            Text(strings.nextQuoteBtn, fontSize = 11.sp, color = FocusLockColors.NormalSky)
                        }
                    }

                    Text(
                        text = if (isBn) activeQuote.titleBn else activeQuote.titleEn,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FocusLockColors.TextPrimary
                    )
                    if (activeQuote.arabicLine != null) {
                        Text(
                            text = activeQuote.arabicLine,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FocusLockColors.FocusEmerald
                        )
                    }
                    Text(
                        text = if (isBn) activeQuote.bodyBn else activeQuote.bodyEn,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = FocusLockColors.TextSecondary
                    )

                    if (state.personalCommitmentNote.isNotBlank()) {
                        HorizontalDivider(color = FocusLockColors.Hairline)
                        Text(
                            text = "${strings.commitmentHeader}: ${state.personalCommitmentNote}",
                            fontSize = 12.sp,
                            color = FocusLockColors.TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(context, BlockReminderActivity::class.java).apply {
                                putExtra(BlockReminderActivity.EXTRA_QUOTE_INDEX, state.activeQuoteIndex)
                            }
                            context.startActivity(intent)
                        },
                        border = BorderStroke(1.dp, FocusLockColors.FocusEmerald.copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.emergencyButtonLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FocusLockColors.FocusEmerald
                        )
                    }
                }
            }
        }

        // Always-Switchable Ads & Tracker Blocker Card (Remains ON/OFF even while Focus Lock & PIN are locked)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(
                1.dp,
                if (state.adsBlockerEnabled) {
                    FocusLockColors.NormalSky.copy(alpha = 0.45f)
                } else {
                    FocusLockColors.WarningAmber.copy(alpha = 0.5f)
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.adsBlockerCardTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FocusLockColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = strings.adsBlockerAlwaysSwitchableBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FocusLockColors.NormalSky
                    )
                }
                Text(
                    text = if (state.adsBlockerEnabled) strings.adsBlockerOnDesc else strings.adsBlockerOffDesc,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = if (state.adsBlockerEnabled) FocusLockColors.TextSecondary else FocusLockColors.WarningAmber
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

        // Pre-Lock Active Rules Checklist (Choose ON/OFF before locking; freezes once locked)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(
                1.dp,
                if (state.timerState.isFocusLockActive) {
                    FocusLockColors.FocusEmerald.copy(alpha = 0.45f)
                } else {
                    FocusLockColors.Hairline
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            val rulesEditable = !state.timerState.isFocusLockActive
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.preLockTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FocusLockColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (!rulesEditable) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = FocusLockColors.FocusEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = if (rulesEditable) strings.preLockUnlockedHint else strings.preLockLockedNote,
                    fontSize = 11.sp,
                    color = if (rulesEditable) FocusLockColors.TextSecondary else FocusLockColors.FocusEmerald
                )

                RuleToggleRow(
                    title = strings.ruleAdultTitle,
                    desc = strings.ruleAdultDesc,
                    checked = state.ruleBlockAdultSites,
                    enabled = rulesEditable,
                    onCheckedChange = { next -> runWithOptionalPin { onSetRuleAdult(next) } }
                )
                HorizontalDivider(color = FocusLockColors.Hairline)
                RuleToggleRow(
                    title = strings.ruleGamblingTitle,
                    desc = strings.ruleGamblingDesc,
                    checked = state.ruleBlockGamblingSites,
                    enabled = rulesEditable,
                    onCheckedChange = { next -> runWithOptionalPin { onSetRuleGambling(next) } }
                )
                HorizontalDivider(color = FocusLockColors.Hairline)
                RuleToggleRow(
                    title = strings.ruleSocialTitle,
                    desc = strings.ruleSocialDesc,
                    checked = state.ruleBlockSocialShorts,
                    enabled = rulesEditable,
                    onCheckedChange = { next -> runWithOptionalPin { onSetRuleSocial(next) } }
                )
                HorizontalDivider(color = FocusLockColors.Hairline)
                RuleToggleRow(
                    title = strings.ruleYouTubeTitle,
                    desc = strings.ruleYouTubeDesc,
                    checked = state.excludeYouTubeApp,
                    enabled = rulesEditable,
                    onCheckedChange = { next -> runWithOptionalPin { onSetExcludeYouTube(next) } }
                )
                HorizontalDivider(color = FocusLockColors.Hairline)
                RuleToggleRow(
                    title = strings.ruleReminderTitle,
                    desc = strings.ruleReminderDesc,
                    checked = state.motivationalReminderEnabled,
                    enabled = rulesEditable,
                    onCheckedChange = { next -> runWithOptionalPin { onSetMotivationalReminder(next) } }
                )
            }
        }

        // Duration Selector
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.durationTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FocusLockColors.TextPrimary
                    )
                    Text(
                        text = "${strings.selectedPrefix} $selectedDurationLabel",
                        fontSize = 12.sp,
                        color = FocusLockColors.FocusEmerald
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val presets = listOf(1, 3, 7, 14, 30)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { days ->
                        val selected = !state.isCustomDurationSelected && state.selectedPresetDays == days
                        DurationChoiceButton(
                            label = "$days ${strings.daysUnit}",
                            selected = selected,
                            enabled = !state.timerState.isFocusLockActive,
                            onClick = { runWithOptionalPin { onSelectPresetDays(days) } }
                        )
                    }
                    DurationChoiceButton(
                        label = strings.customDurationLabel,
                        selected = state.isCustomDurationSelected,
                        enabled = !state.timerState.isFocusLockActive,
                        onClick = { runWithOptionalPin { showCustomDialog = true } }
                    )
                }
            }
        }

        // VPN Connection & Next Renewal Details
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = FocusLockColors.Surface,
            border = BorderStroke(1.dp, FocusLockColors.Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.vpnCardTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusLockColors.TextPrimary
                )

                StatusRow(
                    label = strings.vpnStateLabel,
                    value = when (state.vpnSnapshot.connectionState) {
                        VpnConnectionState.ACTIVE -> strings.vpnStateActive
                        VpnConnectionState.CONNECTING -> strings.vpnStateConnecting
                        VpnConnectionState.REVOKED -> strings.vpnStateRevoked
                        VpnConnectionState.ERROR -> "${strings.vpnStateErrorPrefix} ${state.vpnSnapshot.lastError ?: ""}"
                        VpnConnectionState.DISCONNECTED -> strings.vpnStateDisconnected
                    },
                    valueColor = if (state.isProtectionActuallyRunning) {
                        FocusLockColors.FocusEmerald
                    } else {
                        FocusLockColors.DangerRed
                    }
                )
                HorizontalDivider(color = FocusLockColors.Hairline)

                StatusRow(
                    label = strings.activeDnsLabel,
                    value = "${state.vpnSnapshot.activeProfile.dohHost} (DoH TLS)",
                    valueColor = FocusLockColors.TextPrimary
                )
                HorizontalDivider(color = FocusLockColors.Hairline)

                val nextRenewalText = if (state.timerState.isFocusLockActive) {
                    val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    "${formatter.format(Date(state.timerState.expiresAtEpochMs))} — ${strings.renewalActiveSuffix}"
                } else {
                    strings.renewalNormalText
                }
                StatusRow(
                    label = strings.nextRenewalLabel,
                    value = nextRenewalText,
                    valueColor = FocusLockColors.TextSecondary
                )
            }
        }
    }

    if (showCustomDialog) {
        CustomDurationDialog(
            strings = strings,
            initialDays = state.customDays,
            initialHours = state.customHours,
            onConfirm = onSelectCustomDuration,
            onDismiss = { showCustomDialog = false }
        )
    }

    pendingPinAction?.let { action ->
        VerifyAdminPinDialog(
            strings = strings,
            onVerify = onVerifyPin,
            onDismiss = { pendingPinAction = null },
            onSuccess = {
                pendingPinAction = null
                action()
            }
        )
    }
}

@Composable
private fun DurationChoiceButton(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) FocusLockColors.FocusEmeraldSoft else FocusLockColors.SurfaceElevated
    val borderColor = if (selected) FocusLockColors.FocusEmerald else FocusLockColors.Hairline
    val textColor = when {
        !enabled -> FocusLockColors.TextMuted
        selected -> FocusLockColors.FocusEmerald
        else -> FocusLockColors.TextPrimary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun RuleToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) FocusLockColors.TextPrimary else FocusLockColors.TextSecondary
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = FocusLockColors.TextMuted
            )
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = FocusLockColors.Canvas,
                checkedTrackColor = FocusLockColors.FocusEmerald,
                disabledCheckedTrackColor = FocusLockColors.FocusEmerald.copy(alpha = 0.45f)
            )
        )
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.sp, color = FocusLockColors.TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}
