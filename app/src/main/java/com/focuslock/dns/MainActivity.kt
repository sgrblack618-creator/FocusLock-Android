package com.focuslock.dns

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslock.dns.ui.screens.DashboardScreen
import com.focuslock.dns.ui.screens.SettingsScreen
import com.focuslock.dns.ui.strings.BilingualStrings
import com.focuslock.dns.ui.theme.FocusLockColors
import com.focuslock.dns.ui.theme.FocusLockTheme
import com.focuslock.dns.ui.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FocusLockTheme {
                val viewModel: DashboardViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val strings = BilingualStrings.forCode(uiState.appLanguage)
                var selectedTab by rememberSaveable { mutableIntStateOf(0) }
                var pendingVpnAction by remember { mutableStateOf<(() -> Unit)?>(null) }

                val vpnConsentLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        pendingVpnAction?.invoke()
                    }
                    pendingVpnAction = null
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    viewModel.refreshSystemChecks(granted)
                }

                val lifecycleOwner = LocalLifecycleOwner.current
                LaunchedEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.refreshSystemChecks(hasNotificationPermission())
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !hasNotificationPermission()
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (VpnService.prepare(this@MainActivity) == null) {
                        viewModel.activateNormalModeVpn()
                    }
                }

                val executeWithVpnConsent: (() -> Unit) -> Unit = { action ->
                    val prepareIntent = VpnService.prepare(this@MainActivity)
                    if (prepareIntent == null) {
                        action()
                    } else {
                        pendingVpnAction = action
                        vpnConsentLauncher.launch(prepareIntent)
                    }
                }

                Scaffold(
                    containerColor = FocusLockColors.Canvas,
                    bottomBar = {
                        NavigationBar(
                            containerColor = FocusLockColors.Surface,
                            contentColor = FocusLockColors.TextPrimary
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Rounded.Lock, contentDescription = strings.navDashboard) },
                                label = { Text(strings.navDashboard) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FocusLockColors.FocusEmerald,
                                    selectedTextColor = FocusLockColors.FocusEmerald,
                                    indicatorColor = FocusLockColors.FocusEmeraldSoft,
                                    unselectedIconColor = FocusLockColors.TextSecondary,
                                    unselectedTextColor = FocusLockColors.TextSecondary
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Rounded.Settings, contentDescription = strings.navSettings) },
                                label = { Text(strings.navSettings) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FocusLockColors.FocusEmerald,
                                    selectedTextColor = FocusLockColors.FocusEmerald,
                                    indicatorColor = FocusLockColors.FocusEmeraldSoft,
                                    unselectedIconColor = FocusLockColors.TextSecondary,
                                    unselectedTextColor = FocusLockColors.TextSecondary
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    if (selectedTab == 0) {
                        DashboardScreen(
                            state = uiState,
                            strings = strings,
                            onToggleLanguage = viewModel::toggleLanguage,
                            onNextQuote = viewModel::nextQuote,
                            onSelectPresetDays = viewModel::selectPresetDays,
                            onSelectCustomDuration = viewModel::selectCustomDuration,
                            onRequestStartFocusLock = {
                                executeWithVpnConsent { viewModel.startFocusLockProtection() }
                            },
                            onRequestEnableNormalVpn = {
                                executeWithVpnConsent { viewModel.activateNormalModeVpn() }
                            },
                            onVerifyPin = viewModel::verifyPin,
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        SettingsScreen(
                            state = uiState,
                            strings = strings,
                            onSetPin = viewModel::setPin,
                            onVerifyPin = viewModel::verifyPin,
                            onRemovePin = viewModel::removePin,
                            onSetMotivationalReminder = viewModel::setMotivationalReminderEnabled,
                            onSaveCommitment = viewModel::savePersonalCommitment,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
