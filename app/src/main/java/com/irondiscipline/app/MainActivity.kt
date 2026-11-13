package com.irondiscipline.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.irondiscipline.app.alarm.AlarmRepository
import com.irondiscipline.app.timer.ProTimerManager
import com.irondiscipline.app.timer.ProTimerState
import com.irondiscipline.app.ui.components.jellyElastic
import com.irondiscipline.app.ui.screens.AlarmScreen
import com.irondiscipline.app.ui.screens.AppPasswordGateScreen
import com.irondiscipline.app.ui.screens.DashboardScreen
import com.irondiscipline.app.ui.screens.SettingsScreen
import com.irondiscipline.app.ui.screens.TimerScreen
import com.irondiscipline.app.ui.theme.IronDisciplineTheme
import com.irondiscipline.app.ui.theme.LuxuryBgTop
import com.irondiscipline.app.ui.theme.LuxuryChampagneGold
import com.irondiscipline.app.ui.theme.LuxuryCrimsonAlert
import com.irondiscipline.app.ui.theme.LuxuryNavSpecularBorder
import com.irondiscipline.app.ui.theme.LuxuryNavSurface
import com.irondiscipline.app.ui.theme.LuxurySapphireCyan
import com.irondiscipline.app.ui.theme.LuxuryTacticalEmerald
import com.irondiscipline.app.ui.viewmodel.MainViewModel

/**
 * Primary bottom-navigation tabs in fixed display order.
 */
enum class MainNavTab(
    val label: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    DASHBOARD(
        label = "Dashboard",
        icon = Icons.Default.Shield,
        accentColor = LuxuryChampagneGold
    ),
    ALARM(
        label = "Alarm",
        icon = Icons.Default.Alarm,
        accentColor = LuxurySapphireCyan
    ),
    TIMER(
        label = "Timer",
        icon = Icons.Default.Timer,
        accentColor = LuxuryChampagneGold
    ),
    SETTINGS(
        label = "Settings",
        icon = Icons.Default.Settings,
        accentColor = LuxurySapphireCyan
    )
}

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    companion object {
        var isNavigatingToSystemIntent = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IronDisciplineTheme {
                IronDisciplineMainApp(viewModel = mainViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        IronDisciplineApp.isMainActivityVisible = true
    }

    override fun onPause() {
        IronDisciplineApp.isMainActivityVisible = false
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        if (!isNavigatingToSystemIntent) {
            mainViewModel.lockSession()
        } else {
            isNavigatingToSystemIntent = false
        }
    }
}

@Composable
fun IronDisciplineMainApp(viewModel: MainViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionsAndApps()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val realtimeQuotaState by viewModel.realtimeQuotaState.collectAsState()
    val permissionState by viewModel.permissionState.collectAsState()
    val bannerMessage by viewModel.statusBannerMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableStateOf(MainNavTab.DASHBOARD) }
    val proTimerState by ProTimerManager.timerState.collectAsState()
    val context = LocalContext.current

    // Auto-prompt Device Admin on app launch if not enabled yet
    LaunchedEffect(Unit) {
        val dpm = context.getSystemService(android.content.Context.DEVICE_POLICY_SERVICE) as? android.app.admin.DevicePolicyManager
        val adminComponent = android.content.ComponentName(context, FocusDeviceAdminReceiver::class.java)
        if (dpm != null && !dpm.isAdminActive(adminComponent)) {
            try {
                val intent = android.content.Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                    putExtra(
                        android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Prevents accidental uninstallation without your 4-digit PIN."
                    )
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(bannerMessage) {
        val msg = bannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBannerMessage()
        }
    }

    val isMasterPasswordEnabled by viewModel.isMasterPasswordEnabled.collectAsState()
    val isAppUnlockedSession by viewModel.isAppUnlockedSession.collectAsState()
    val customPasswordLockedPackages by viewModel.customPasswordLockedPackages.collectAsState()

    if (isMasterPasswordEnabled && !isAppUnlockedSession) {
        AppPasswordGateScreen(
            onUnlockSuccess = {
                viewModel.unlockSession()
            },
            onVerifyPassword = { entered ->
                viewModel.verifyMasterPassword(entered)
            },
            onExitToHome = {
                (context as? ComponentActivity)?.moveTaskToBack(true)
            }
        )
        return
    }

    Scaffold(
        containerColor = LuxuryBgTop,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(LuxuryNavSpecularBorder)
                )
                NavigationBar(
                    containerColor = LuxuryNavSurface
                ) {
                    MainNavTab.entries.forEach { tab ->
                        NavigationBarItem(
                            modifier = Modifier.jellyElastic(
                                enabled = true,
                                baseCornerRadius = 24.dp,
                                maxTranslationDp = 16.dp,
                                maxStretchScale = 0.25f
                            ),
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                if (tab == MainNavTab.TIMER &&
                                    (proTimerState == ProTimerState.RUNNING || proTimerState == ProTimerState.FINISHED_RINGING)
                                ) {
                                    Box {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (proTimerState == ProTimerState.FINISHED_RINGING) {
                                                        LuxuryCrimsonAlert
                                                    } else {
                                                        LuxuryTacticalEmerald
                                                    }
                                                )
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = tab.accentColor,
                                selectedTextColor = tab.accentColor,
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B),
                                indicatorColor = LuxurySapphireCyan.copy(alpha = 0.14f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val alarmRepository = remember { AlarmRepository.getInstance(context) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            when (selectedTab) {
                MainNavTab.DASHBOARD -> DashboardScreen(
                    realtimeQuotaState = realtimeQuotaState,
                    permissionState = permissionState,
                    isMasterPasswordEnabled = isMasterPasswordEnabled,
                    lockedPackages = customPasswordLockedPackages,
                    onSetMasterPassword = viewModel::setMasterPassword,
                    onToggleCustomPackageLock = viewModel::toggleCustomPackageLock,
                    onLoadInstalledApps = viewModel::loadInstalledLauncherApps,
                    onActivateOneMinuteTestMode = viewModel::activateOneMinuteTestMode,
                    onSwitchBackToNormalThirtyMinMode = viewModel::switchBackToNormalThirtyMinModeAndReset,
                    onRefreshPermissions = viewModel::refreshPermissionsAndApps,
                    onVerifyPin = viewModel::verifyUninstallPin,
                    onConfirmUninstall = viewModel::executeAuthorizedUninstall
                )
                MainNavTab.ALARM -> AlarmScreen(
                    alarmRepository = alarmRepository
                )
                MainNavTab.TIMER -> TimerScreen()
                MainNavTab.SETTINGS -> SettingsScreen(
                    permissionState = permissionState,
                    onRefreshPermissions = viewModel::refreshPermissionsAndApps,
                    onVerifyPin = viewModel::verifyUninstallPin,
                    onConfirmUninstall = viewModel::executeAuthorizedUninstall,
                    onChangePin = viewModel::updateUninstallPin
                )
            }
        }
    }
}
