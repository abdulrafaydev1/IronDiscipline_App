package com.irondiscipline.app.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.irondiscipline.app.FocusDeviceAdminReceiver
import com.irondiscipline.app.domain.model.PermissionStatusState
import com.irondiscipline.app.ui.components.LuxuryBrushes
import com.irondiscipline.app.ui.components.LuxuryGlassCard
import com.irondiscipline.app.ui.components.LuxurySectionHeader
import com.irondiscipline.app.ui.components.LuxuryStatusPill
import com.irondiscipline.app.ui.components.jellyElastic
import com.irondiscipline.app.ui.theme.LuxuryChampagneGold
import com.irondiscipline.app.ui.theme.LuxuryCrimsonAlert
import com.irondiscipline.app.ui.theme.LuxuryPlatinumSubtitle
import com.irondiscipline.app.ui.theme.LuxurySapphireCyan
import com.irondiscipline.app.ui.theme.LuxuryTacticalEmerald
import com.irondiscipline.app.util.CoreAppQuotaStatus
import com.irondiscipline.app.util.CoreTargetApp
import com.irondiscipline.app.util.PermissionHelper
import com.irondiscipline.app.util.RealtimeQuotaManager
import com.irondiscipline.app.util.RealtimeQuotaSnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

// Obsidian Cyber-Discipline Palette Tokens
private val ColorObsidianDeep = Color(0xFF06080E)
private val ColorCyberAmber = Color(0xFFFFB300)
private val ColorAmberDark = Color(0xFFF59E0B)
private val ColorCrimsonDark = Color(0xFFDC2626)

private val ColorTextPrimary = Color(0xFFFFFFFF)
private val ColorTextSecondary = Color(0xFF8A99AD)

// App Brand Gradient Definitions
private val YouTubeGradient = listOf(Color(0xFFFF0033), Color(0xFFCC0000))
private val InstagramGradient = listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF))
private val FacebookGradient = listOf(Color(0xFF1877F2), Color(0xFF0C5DC7))
private val TikTokGradient = listOf(Color(0xFF00F2FE), Color(0xFFFE0979))

@Composable
fun DashboardScreen(
    realtimeQuotaState: RealtimeQuotaSnapshot,
    permissionState: PermissionStatusState,
    onOpenSidebar: () -> Unit = {},
    onRefreshPermissions: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val quotaManager = remember(context) { RealtimeQuotaManager.getInstance(context) }
    val penaltyPrefs = remember(context) {
        context.getSharedPreferences(RealtimeQuotaManager.PREFS_NAME, Context.MODE_PRIVATE)
    }
    var liveNowEpochMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Recalculate wall-clock lock remaining time every 500ms while visible and immediately on resume
    LaunchedEffect(quotaManager) {
        while (isActive) {
            val nowMs = System.currentTimeMillis()
            liveNowEpochMs = nowMs
            quotaManager.refreshSnapshotNow(nowMs)
            delay(500L)
        }
    }

    DisposableEffect(lifecycleOwner, quotaManager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val nowMs = System.currentTimeMillis()
                liveNowEpochMs = nowMs
                quotaManager.refreshSnapshotNow(nowMs)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val scrollState = rememberScrollState()
    val isTestMode = realtimeQuotaState.isTestModeActive

    // Calculate aggregated daily statistics for the Hero Gauge
    val totalLimitSeconds = realtimeQuotaState.coreStatuses.sumOf { it.limitSeconds }.coerceAtLeast(1)
    val totalUsedSeconds = realtimeQuotaState.coreStatuses.sumOf { it.usedSeconds }
    val totalRemainingSeconds = (totalLimitSeconds - totalUsedSeconds).coerceAtLeast(0)
    val remainingFraction = (totalRemainingSeconds.toFloat() / totalLimitSeconds.toFloat()).coerceIn(0f, 1f)
    val remainingPercent = (remainingFraction * 100).toInt()
    val unlockedCount = realtimeQuotaState.coreStatuses.count { !it.isLocked }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBrushes.primaryBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val dpm = remember { context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager }
        val adminComponent = remember { ComponentName(context, FocusDeviceAdminReceiver::class.java) }
        val isDeviceAdminActive = remember(permissionState) {
            try { dpm?.isAdminActive(adminComponent) == true } catch (_: Exception) { false }
        }

        // 1. Top Executive Header Card with Circular Sidebar Button & Animated Guard Status Pill
        CommandHeaderSection(
            permissionState = permissionState,
            onOpenSidebar = onOpenSidebar,
            onEnableGuardClick = {
                try {
                    context.startActivity(PermissionHelper.createAccessibilitySettingsIntent())
                } catch (_: Exception) {}
            }
        )

        // High-Visibility Banner: Prompt Device Admin if not enabled yet
        if (!isDeviceAdminActive) {
            DeviceAdminSetupBannerCard(
                onActivateClick = {
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                        putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "Prevents accidental uninstallation without your 4-digit PIN."
                        )
                    }
                    context.startActivity(intent)
                }
            )
        }

        // 2. Hero Motivational & Daily Focus Banner Card (with Circular Discipline Gauge)
        HeroDisciplineBannerCard(
            unlockedCount = unlockedCount,
            totalAppsCount = CoreTargetApp.entries.size,
            remainingFraction = remainingFraction,
            remainingPercent = remainingPercent,
            isTestMode = isTestMode
        )

        // 3. Sleek Permission Alert Card (Conditional: shown if critical permissions or Device Admin missing)
        if (!permissionState.allCriticalGranted || !permissionState.isDeviceAdminActive) {
            SleekPermissionAlertCard(
                context = context,
                permissionState = permissionState,
                onRefreshPermissions = onRefreshPermissions
            )
        }

        // 4. Daily Social Media Quotas (YouTube, Facebook, Instagram, TikTok)
        LuxurySectionHeader(
            title = if (isTestMode) "DAILY SOCIAL MEDIA QUOTAS (1M TEST)" else "DAILY SOCIAL MEDIA QUOTAS (30M LIMIT)",
            trailingText = if (isTestMode) "DIAGNOSTIC 1-MIN" else "STRICT 30-MIN",
            trailingColor = if (isTestMode) LuxuryChampagneGold else LuxuryTacticalEmerald
        )

        for (target in CoreTargetApp.entries) {
            val baseStatus = realtimeQuotaState.statusFor(target.appKey) ?: CoreAppQuotaStatus(
                target = target,
                usedSeconds = 0,
                limitSeconds = if (isTestMode) 60 else 1800,
                remainingSeconds = if (isTestMode) 60 else 1800,
                isLocked = false,
                lockedUntilEpochMs = 0L,
                lockRemainingMillis = 0L
            )
            val wallClockLockMs = target.packageNames.maxOfOrNull { pkg ->
                RealtimeQuotaManager.getRemainingPenaltyLockMillisAt(
                    prefs = penaltyPrefs,
                    packageName = pkg,
                    nowEpochMs = liveNowEpochMs
                )
            } ?: 0L
            val effectiveLockMs = maxOf(
                wallClockLockMs,
                if (baseStatus.lockedUntilEpochMs > liveNowEpochMs) {
                    baseStatus.lockedUntilEpochMs - liveNowEpochMs
                } else {
                    0L
                }
            )
            val status = if (effectiveLockMs > 0L) {
                baseStatus.copy(
                    isLocked = true,
                    lockRemainingMillis = effectiveLockMs,
                    remainingLockSeconds = ((effectiveLockMs + 999L) / 1000L).toInt()
                )
            } else {
                baseStatus
            }

            PremiumAppDisciplineCard(
                status = status,
                isTestMode = isTestMode
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * 1. Top Executive Header Card with Circular Sidebar Button, metallic typography & pulsating Guard Status Pill.
 */
@Composable
private fun CommandHeaderSection(
    permissionState: PermissionStatusState,
    onOpenSidebar: () -> Unit,
    onEnableGuardClick: () -> Unit
) {
    val isGuardActive = permissionState.isAccessibilityServiceEnabled

    // Pulsing alpha animation for disabled guard warning indicator
    val infiniteTransition = rememberInfiniteTransition(label = "GuardPulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    LuxuryGlassCard(
        cornerRadius = 22.dp,
        borderBrush = if (isGuardActive) LuxuryBrushes.specularBorder else LuxuryBrushes.activeHighlightBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Sleek 44.dp Circular Sidebar Trigger Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF151D2C), Color(0xFF0E1420))
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(LuxuryChampagneGold, LuxurySapphireCyan)
                            ),
                            shape = CircleShape
                        )
                        .jellyElastic(baseCornerRadius = 22.dp)
                        .clickable(onClick = onOpenSidebar),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Sidebar",
                        tint = LuxuryChampagneGold,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "IRON DISCIPLINE",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.White, LuxuryChampagneGold)
                            )
                        ),
                        fontWeight = FontWeight.Black,
                        fontSize = 19.sp,
                        letterSpacing = 1.2.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "EXECUTIVE FOCUS & SECURITY",
                        color = LuxurySapphireCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.0.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            LuxuryStatusPill(
                text = if (isGuardActive) "🛡️ ACTIVE GUARD" else "⚠️ ENABLE GUARD",
                accentColor = if (isGuardActive) LuxuryTacticalEmerald else Color(0xFFFFB300),
                showDot = !isGuardActive,
                dotAlpha = if (isGuardActive) 1f else dotAlpha,
                modifier = if (!isGuardActive) {
                    Modifier.clickable(onClick = onEnableGuardClick)
                } else {
                    Modifier
                }
            )
        }
    }
}

/**
 * 2. Hero Motivational & Daily Focus Banner Card with Circular Discipline Gauge.
 */
@Composable
private fun HeroDisciplineBannerCard(
    unlockedCount: Int,
    totalAppsCount: Int,
    remainingFraction: Float,
    remainingPercent: Int,
    isTestMode: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = remainingFraction,
        animationSpec = tween(durationMillis = 700),
        label = "HeroGaugeProgress"
    )

    LuxuryGlassCard(
        cornerRadius = 22.dp,
        borderBrush = LuxuryBrushes.activeHighlightBorder
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Motivational Copy & Stats
                Column(modifier = Modifier.weight(1f)) {
                    LuxuryStatusPill(
                        text = if (isTestMode) "DIAGNOSTIC 1-MIN MODE" else "MINDSET OVER IMPULSE",
                        accentColor = LuxuryChampagneGold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Guard Your Time.\nBuild Your Empire.",
                        color = ColorTextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        lineHeight = 25.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$unlockedCount OF $totalAppsCount APPS UNLOCKED • FOCUS STRONG",
                        color = LuxuryPlatinumSubtitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right Column: Circular Discipline Gauge
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(76.dp)) {
                        val strokeWidth = 7.dp.toPx()

                        // Background ring track
                        drawArc(
                            color = Color(0xFF1F2B40),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Glowing sweep progress (Champagne Gold -> Electric Cyan -> Emerald)
                        val gaugeGradient = Brush.sweepGradient(
                            listOf(
                                LuxuryChampagneGold,
                                LuxurySapphireCyan,
                                LuxuryTacticalEmerald,
                                LuxuryChampagneGold
                            )
                        )

                        drawArc(
                            brush = gaugeGradient,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$remainingPercent%",
                            color = ColorTextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "LEFT",
                            color = LuxuryPlatinumSubtitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Sleek Permission Alert Card (Conditional).
 */
@Composable
private fun SleekPermissionAlertCard(
    context: Context,
    permissionState: PermissionStatusState,
    onRefreshPermissions: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(ColorCyberAmber, ColorAmberDark)
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF22170A), Color(0xFF160E06))
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = ColorCyberAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Shield Permissions Required",
                            color = ColorTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Enable required permissions to detect & lock distracting apps.",
                            color = ColorCyberAmber.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!permissionState.isAccessibilityServiceEnabled) {
                    Button(
                        onClick = {
                            context.startActivity(PermissionHelper.createAccessibilitySettingsIntent())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .jellyElastic(baseCornerRadius = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorCyberAmber,
                            contentColor = ColorObsidianDeep
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessibilityNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enable Accessibility Service",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!permissionState.hasOverlayPermission) {
                    Button(
                        onClick = {
                            context.startActivity(PermissionHelper.createOverlayPermissionIntent(context))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .jellyElastic(baseCornerRadius = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00F5D4),
                            contentColor = ColorObsidianDeep
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enable Overlay Permission",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!permissionState.isDeviceAdminActive) {
                    Button(
                        onClick = {
                            context.startActivity(PermissionHelper.createEnableDeviceAdminIntent(context))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .jellyElastic(baseCornerRadius = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF38BDF8),
                            contentColor = ColorObsidianDeep
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🛡️ Enable Device Admin (Uninstall Protection)",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onRefreshPermissions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .jellyElastic(baseCornerRadius = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B4860))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = ColorTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Verify Permission Status",
                        color = ColorTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * 5. Premium Per-App Flagship Cards (YouTube, Facebook, Instagram, TikTok).
 */
@Composable
private fun PremiumAppDisciplineCard(
    status: CoreAppQuotaStatus,
    isTestMode: Boolean
) {
    val isLocked = status.isLocked

    val brandGradient = when (status.target) {
        CoreTargetApp.YOUTUBE -> YouTubeGradient
        CoreTargetApp.INSTAGRAM -> InstagramGradient
        CoreTargetApp.FACEBOOK -> FacebookGradient
        CoreTargetApp.TIKTOK -> TikTokGradient
    }

    val monogramText = when (status.target) {
        CoreTargetApp.YOUTUBE -> "YT"
        CoreTargetApp.INSTAGRAM -> "IG"
        CoreTargetApp.FACEBOOK -> "FB"
        CoreTargetApp.TIKTOK -> "TT"
    }

    val cardBorderBrush = if (isLocked) {
        Brush.linearGradient(listOf(LuxuryCrimsonAlert, Color(0xFF991B1B)))
    } else if (status.progressFraction >= 0.7f) {
        Brush.linearGradient(listOf(LuxuryChampagneGold, Color(0xFF2E3D59)))
    } else {
        LuxuryBrushes.specularBorder
    }

    LuxuryGlassCard(
        cornerRadius = 20.dp,
        borderBrush = cardBorderBrush,
        borderWidth = if (isLocked) 1.4.dp else 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Circular tinted icon badge + App Name + Status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(brandGradient.map { it.copy(alpha = 0.24f) }))
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(brandGradient),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monogramText,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = status.target.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LuxuryStatusPill(
                            text = if (isLocked) "🔒 LOCKED • 5H PENALTY" else "ACTIVE QUOTA",
                            accentColor = when {
                                isLocked -> LuxuryCrimsonAlert
                                status.progressFraction >= 0.7f -> LuxuryChampagneGold
                                else -> LuxurySapphireCyan
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Right: Large tabular monospace countdown + used indicator
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isLocked) status.formattedLockRemainingHhMmSs else status.formattedRemainingMmSs,
                        color = when {
                            isLocked -> LuxuryCrimsonAlert
                            status.progressFraction >= 0.7f -> LuxuryChampagneGold
                            else -> LuxuryTacticalEmerald
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isLocked) "REMAINING LOCK" else "${status.formattedUsedMmSs} / ${if (isTestMode) "01:00" else "30:00"}",
                        color = LuxuryPlatinumSubtitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom: Smooth 8.dp rounded gradient progress bar (Emerald/Cyan -> Amber -> Crimson)
            val animatedFraction by animateFloatAsState(
                targetValue = status.progressFraction,
                animationSpec = tween(500),
                label = "AppProgress"
            )

            val fillBrush = when {
                isLocked || status.progressFraction >= 0.85f -> Brush.horizontalGradient(
                    listOf(LuxuryCrimsonAlert, ColorCrimsonDark)
                )
                status.progressFraction >= 0.55f -> Brush.horizontalGradient(
                    listOf(LuxuryChampagneGold, ColorAmberDark)
                )
                else -> Brush.horizontalGradient(
                    listOf(LuxuryTacticalEmerald, LuxurySapphireCyan)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0A0E17))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedFraction.coerceIn(0.02f, 1f))
                        .clip(RoundedCornerShape(4.dp))
                        .background(fillBrush)
                )
            }
        }
    }
}

/**
 * High-Visibility Banner Card prompting the user to enable Device Admin Uninstall Protection.
 */
@Composable
fun DeviceAdminSetupBannerCard(
    onActivateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "🛡️ Step Required: Enable Uninstall Protection",
                        color = ColorTextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Prevents accidental uninstallation without entering your 4-digit PIN.",
                        color = Color(0xFF38BDF8).copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onActivateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .jellyElastic(baseCornerRadius = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38BDF8),
                    contentColor = ColorObsidianDeep
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Activate Protection Shield",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}
