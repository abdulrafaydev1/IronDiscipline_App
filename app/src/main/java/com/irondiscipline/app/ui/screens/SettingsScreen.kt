package com.irondiscipline.app.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irondiscipline.app.domain.model.PermissionStatusState
import com.irondiscipline.app.ui.components.ChangeSecurityPinDialog
import com.irondiscipline.app.ui.components.LuxuryBrushes
import com.irondiscipline.app.ui.components.LuxuryGlassCard
import com.irondiscipline.app.ui.components.LuxurySectionHeader
import com.irondiscipline.app.ui.components.LuxuryStatusPill
import com.irondiscipline.app.ui.components.UninstallPinDialog
import com.irondiscipline.app.ui.components.jellyElastic
import com.irondiscipline.app.ui.theme.LuxuryChampagneGold
import com.irondiscipline.app.ui.theme.LuxuryCrimsonAlert
import com.irondiscipline.app.ui.theme.LuxuryPlatinumSubtitle
import com.irondiscipline.app.ui.theme.LuxurySapphireCyan
import com.irondiscipline.app.ui.theme.LuxuryTacticalEmerald
import com.irondiscipline.app.util.PermissionHelper

// Obsidian Palette
private val ColorObsidianDeep = Color(0xFF06080E)
private val ColorObsidianSlate = Color(0xFF0D121D)
private val ColorElevatedSlate = Color(0xFF141B29)
private val ColorCardBorderStart = Color(0xFF26334D)
private val ColorCardBorderEnd = Color(0xFF141B29)

private val ColorElectricEmerald = Color(0xFF00E676)
private val ColorCyberAmber = Color(0xFFFFB300)
private val ColorAmberDark = Color(0xFFF59E0B)
private val ColorSkyBlue = Color(0xFF38BDF8)

private val ColorTextPrimary = Color(0xFFFFFFFF)
private val ColorTextSecondary = Color(0xFF8A99AD)
private val ColorTextMuted = Color(0xFF5E6D82)

@Composable
fun SettingsScreen(
    permissionState: PermissionStatusState,
    onRefreshPermissions: () -> Unit,
    onVerifyPin: (String) -> Boolean = { true },
    onConfirmUninstall: () -> Unit = {},
    onChangePin: (String, String) -> Boolean = { _, _ -> true }
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showUninstallPinDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    val prefs = remember { context.getSharedPreferences("iron_discipline_prefs", Context.MODE_PRIVATE) }
    var devTestDismissEnabled by remember {
        mutableStateOf(prefs.getBoolean("enable_dev_test_dismiss", true))
    }

    if (showUninstallPinDialog) {
        UninstallPinDialog(
            onDismiss = { showUninstallPinDialog = false },
            onVerifyPin = onVerifyPin,
            onConfirmUninstall = {
                showUninstallPinDialog = false
                onConfirmUninstall()
            }
        )
    }

    if (showChangePinDialog) {
        ChangeSecurityPinDialog(
            onDismiss = { showChangePinDialog = false },
            onVerifyCurrentPin = onVerifyPin,
            onSaveNewPin = onChangePin
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBrushes.primaryBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Executive Settings Header Card
        LuxuryGlassCard(
            cornerRadius = 24.dp,
            borderBrush = LuxuryBrushes.activeHighlightBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SYSTEM SETTINGS",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.White, LuxuryChampagneGold)
                            ),
                            fontWeight = FontWeight.Black,
                            fontSize = 21.sp,
                            letterSpacing = 1.8.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "ACCESSIBILITY, DEVICE ADMIN & SECURITY SUITE",
                        color = LuxuryPlatinumSubtitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.1.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                LuxuryStatusPill(
                    text = if (permissionState.allCriticalGranted) "🛡️ ARMED" else "⚠️ SETUP",
                    accentColor = if (permissionState.allCriticalGranted) {
                        LuxuryTacticalEmerald
                    } else {
                        LuxuryChampagneGold
                    },
                    showDot = true
                )
            }
        }

        // 2. Section: System Permissions Status
        LuxurySectionHeader(
            title = "SHIELD PERMISSION PROTOCOLS",
            trailingText = if (permissionState.allCriticalGranted) "ALL CRITICAL ARMED" else "ACTION REQUIRED",
            trailingColor = if (permissionState.allCriticalGranted) {
                LuxuryTacticalEmerald
            } else {
                LuxuryChampagneGold
            }
        )

        // Permission Card 1: Accessibility Service
        PermissionItemCard(
            title = "Accessibility Service",
            description = "Required for real-time foreground package detection and zero-delay block screen enforcement.",
            icon = Icons.Default.AccessibilityNew,
            isGranted = permissionState.isAccessibilityServiceEnabled,
            actionLabel = "Enable Accessibility",
            onActionClick = {
                context.startActivity(PermissionHelper.createAccessibilitySettingsIntent())
            }
        )

        // Permission Card 2: Display Over Other Apps (Overlay)
        PermissionItemCard(
            title = "Display Over Other Apps (Overlay)",
            description = "Allows IronDiscipline to draw the full-screen vault overlay over distracting apps when limits expire.",
            icon = Icons.Default.Layers,
            isGranted = permissionState.hasOverlayPermission,
            actionLabel = "Enable Overlay Permission",
            onActionClick = {
                context.startActivity(PermissionHelper.createOverlayPermissionIntent(context))
            }
        )

        // Permission Card 3: Usage Access Permission
        PermissionItemCard(
            title = "Usage Access Permission",
            description = "Provides accurate daily app usage metrics and foreground session tracking.",
            icon = Icons.Default.QueryStats,
            isGranted = permissionState.hasUsageStatsPermission,
            actionLabel = "Grant Usage Access",
            onActionClick = {
                try {
                    context.startActivity(PermissionHelper.createUsageAccessIntent(context))
                } catch (_: Exception) {
                    context.startActivity(PermissionHelper.createFallbackUsageAccessIntent())
                }
            }
        )

        // Permission Card 4: Ignore Battery Optimizations
        PermissionItemCard(
            title = "Ignore Battery Optimizations",
            description = "Exempts IronDiscipline from system battery killers so protection is never halted.",
            icon = Icons.Default.BatteryChargingFull,
            isGranted = permissionState.isBatteryOptimizationExempt,
            actionLabel = "Disable Battery Optimization",
            onActionClick = {
                context.startActivity(PermissionHelper.createBatteryOptimizationExemptionIntent(context))
            }
        )

        // Permission Card 5: Exact Alarm Scheduling
        val isExactAlarmGranted = remember(permissionState) {
            PermissionHelper.canScheduleExactAlarms(context)
        }
        PermissionItemCard(
            title = "Exact Alarm Scheduling",
            description = "Allows waking your device precisely at scheduled alarm times even in deep sleep.",
            icon = Icons.Default.Alarm,
            isGranted = isExactAlarmGranted,
            actionLabel = "Grant Exact Alarm Access",
            onActionClick = {
                try {
                    context.startActivity(PermissionHelper.createRequestExactAlarmIntent(context))
                } catch (_: Exception) {}
            }
        )

        // Permission Card 6: Full-Screen Lock Screen Popup (Android 14+)
        val isFullScreenGranted = remember(permissionState) {
            PermissionHelper.canUseFullScreenIntent(context)
        }
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            PermissionItemCard(
                title = "Full-Screen Lock Intent",
                description = "Required on Android 14+ to display the Math Hard alarm directly over your locked screen.",
                icon = Icons.Default.Shield,
                isGranted = isFullScreenGranted,
                actionLabel = "Authorize Lock Screen Popup",
                onActionClick = {
                    try {
                        context.startActivity(PermissionHelper.createManageFullScreenIntent(context))
                    } catch (_: Exception) {}
                }
            )
        }

        // Permission Card 7: Device Admin Protection
        PermissionItemCard(
            title = "Device Admin Protection",
            description = "Prevents accidental uninstallation from home screen without entering your 4-digit PIN in-app.",
            icon = Icons.Default.Security,
            isGranted = permissionState.isDeviceAdminActive,
            actionLabel = "Activate Device Admin",
            onActionClick = {
                try {
                    context.startActivity(PermissionHelper.createEnableDeviceAdminIntent(context))
                } catch (_: Exception) {}
            }
        )

        // Refresh Permissions Button
        OutlinedButton(
            onClick = onRefreshPermissions,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .jellyElastic(baseCornerRadius = 14.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, LuxurySapphireCyan.copy(alpha = 0.45f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF0C1424),
                contentColor = LuxurySapphireCyan
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = LuxurySapphireCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Verify & Refresh Permission Status",
                color = LuxurySapphireCyan,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }

        // 3. Core Protection Protocols Summary Card
        LuxuryGlassCard(cornerRadius = 22.dp) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = LuxuryTacticalEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CORE PROTECTION PROTOCOLS",
                        color = LuxuryChampagneGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                ProtocolFeatureItem(
                    title = "Strict Per-App Quotas",
                    detail = "Hardcoded 30-Minute independent allowance per application (YouTube, Instagram, Facebook, TikTok)."
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProtocolFeatureItem(
                    title = "Audio & Video Silencing",
                    detail = "Instant exclusive audio focus gain + hardware media pause keys + STREAM_MUSIC mute upon app lock."
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProtocolFeatureItem(
                    title = "Anti-Bypass Vault Overlay",
                    detail = "Programmatic native WindowManager overlay with live countdown timer and home-screen only exit."
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProtocolFeatureItem(
                    title = "Persistent Background Engine",
                    detail = "Low-overhead continuous monitoring service guarded against system kills and idle teardown."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Version Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF080C16))
                        .border(1.dp, LuxuryBrushes.specularBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "IronDiscipline • v1.0.0 (Production Engine)",
                            color = LuxuryPlatinumSubtitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Jetpack Compose M3 • Room DB • Target SDK 34",
                            color = ColorTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 4. Emergency & Testing Controls Card (PIN Protected Uninstall & Security PIN Management)
        LuxuryGlassCard(
            cornerRadius = 22.dp,
            borderBrush = Brush.linearGradient(
                listOf(LuxuryCrimsonAlert.copy(alpha = 0.6f), ColorCardBorderEnd)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = LuxuryCrimsonAlert,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EMERGENCY & TESTING CONTROLS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PIN-guarded controls to disarm protection protocols or cleanly remove the application during development and testing.",
                    color = LuxuryPlatinumSubtitle,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Delete / Uninstall App Button
                Button(
                    onClick = { showUninstallPinDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .jellyElastic(baseCornerRadius = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LuxuryCrimsonAlert.copy(alpha = 0.14f),
                        contentColor = LuxuryCrimsonAlert
                    ),
                    border = BorderStroke(1.2.dp, LuxuryCrimsonAlert.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🗑️ Delete / Uninstall App (PIN Required)",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Change Security PIN Button
                OutlinedButton(
                    onClick = { showChangePinDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .jellyElastic(baseCornerRadius = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, LuxuryChampagneGold.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF0C1322),
                        contentColor = LuxuryChampagneGold
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔐 Change Security PIN (Default: 1234)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Developer Test Dismiss Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0B111E))
                        .border(1.dp, LuxuryBrushes.specularBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🛠️ Dev Test Dismiss on Alarm",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Shows instant bypass button on alarm screen for developer debugging.",
                            color = LuxuryPlatinumSubtitle,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = devTestDismissEnabled,
                        onCheckedChange = { isChecked ->
                            devTestDismissEnabled = isChecked
                            prefs.edit().putBoolean("enable_dev_test_dismiss", isChecked).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF06080E),
                            checkedTrackColor = LuxuryChampagneGold,
                            uncheckedThumbColor = LuxuryPlatinumSubtitle,
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Individual Permission Status Card with direct action button.
 */
@Composable
private fun PermissionItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    LuxuryGlassCard(
        cornerRadius = 22.dp,
        borderBrush = if (isGranted) {
            LuxuryBrushes.specularBorder
        } else {
            Brush.linearGradient(listOf(LuxuryChampagneGold, LuxurySapphireCyan))
        }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isGranted) LuxuryTacticalEmerald.copy(alpha = 0.14f)
                                else LuxuryChampagneGold.copy(alpha = 0.14f)
                            )
                            .border(
                                1.dp,
                                if (isGranted) LuxuryTacticalEmerald.copy(alpha = 0.35f)
                                else LuxuryChampagneGold.copy(alpha = 0.45f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) LuxuryTacticalEmerald else LuxuryChampagneGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                LuxuryStatusPill(
                    text = if (isGranted) "ACTIVE" else "ACTION REQUIRED",
                    accentColor = if (isGranted) LuxuryTacticalEmerald else LuxuryChampagneGold,
                    showDot = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                color = LuxuryPlatinumSubtitle,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!isGranted) {
                Button(
                    onClick = onActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .jellyElastic(baseCornerRadius = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LuxuryChampagneGold,
                        contentColor = Color(0xFF05070B)
                    )
                ) {
                    Text(
                        text = actionLabel,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LuxuryTacticalEmerald.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            LuxuryTacticalEmerald.copy(alpha = 0.25f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LuxuryTacticalEmerald,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Permission granted & active",
                            color = LuxuryTacticalEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtocolFeatureItem(
    title: String,
    detail: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(LuxuryTacticalEmerald)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = detail,
                color = LuxuryPlatinumSubtitle,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
