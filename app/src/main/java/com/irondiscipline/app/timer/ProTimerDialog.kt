package com.irondiscipline.app.timer

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.irondiscipline.app.ui.components.LuxuryBrushes
import com.irondiscipline.app.ui.components.LuxuryGlassCard
import com.irondiscipline.app.ui.components.LuxuryStatusPill
import com.irondiscipline.app.ui.components.jellyElastic
import com.irondiscipline.app.ui.theme.LuxuryChampagneGold
import com.irondiscipline.app.ui.theme.LuxuryCrimsonAlert
import com.irondiscipline.app.ui.theme.LuxuryPlatinumSubtitle
import com.irondiscipline.app.ui.theme.LuxurySapphireCyan
import com.irondiscipline.app.ui.theme.LuxuryTacticalEmerald

/**
 * Full-Screen Obsidian Dark-Luxury Pro Countdown Timer Dialog.
 * Encompasses 3 Core Capabilities and all 10 Built-In Pro Enhancements.
 */
@Composable
fun ProTimerDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ProTimerContent(
            showCloseButton = true,
            onClose = onDismiss
        )
    }
}

/**
 * Shared Pro Countdown Timer UI usable both as a full-screen navigation tab ([TimerScreen])
 * and inside [ProTimerDialog]. Preserves live state via [ProTimerManager].
 */
@Composable
fun ProTimerContent(
    showCloseButton: Boolean = false,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val timerState by ProTimerManager.timerState.collectAsState()
    val totalDurationMs by ProTimerManager.totalDurationMs.collectAsState()
    val remainingMs by ProTimerManager.remainingMs.collectAsState()
    val overtimeMs by ProTimerManager.overtimeMs.collectAsState()
    val selectedTopic by ProTimerManager.selectedTopic.collectAsState()
    val selectedSoundId by ProTimerManager.selectedSoundId.collectAsState()
    val timerLabel by ProTimerManager.timerLabel.collectAsState()
    val isLoopEnabled by ProTimerManager.isLoopEnabled.collectAsState()
    val completedCycles by ProTimerManager.completedCycles.collectAsState()
    val isFinalTenSecBeepEnabled by ProTimerManager.isFinalTenSecBeepEnabled.collectAsState()
    val isKeepScreenOnEnabled by ProTimerManager.isKeepScreenOnEnabled.collectAsState()
    val laps by ProTimerManager.laps.collectAsState()
    val customPresets by ProTimerManager.customPresets.collectAsState()

    // Extra 9: Keep Screen Awake window flag handler
    DisposableEffect(isKeepScreenOnEnabled) {
        if (isKeepScreenOnEnabled) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Steppers state for IDLE time setting
    val initialTotalSec = totalDurationMs / 1000L
    var inputHours by remember(totalDurationMs) { mutableIntStateOf((initialTotalSec / 3600L).toInt()) }
    var inputMinutes by remember(totalDurationMs) { mutableIntStateOf(((initialTotalSec % 3600L) / 60L).toInt()) }
    var inputSeconds by remember(totalDurationMs) { mutableIntStateOf((initialTotalSec % 60L).toInt()) }

    // Pulsing animation for finished ringing state
    val infiniteTransition = rememberInfiniteTransition(label = "RingingPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Progress percentage
    val progress = if (totalDurationMs > 0L) {
        (remainingMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Extra 4: Dynamic Color Shift (Cyan -> Champagne Gold -> Crimson)
    val arcColor by animateColorAsState(
        targetValue = when {
            timerState == ProTimerState.FINISHED_RINGING -> LuxuryCrimsonAlert
            progress > 0.5f -> LuxurySapphireCyan
            progress in 0.2f..0.5f -> LuxuryChampagneGold
            else -> LuxuryCrimsonAlert
        },
        label = "ArcColorShift"
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBrushes.primaryBackground)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Luxury Executive Header Card
            LuxuryGlassCard(
                cornerRadius = 22.dp,
                borderBrush = LuxuryBrushes.specularBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱️ PRO COUNTDOWN TIMER",
                                style = TextStyle(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color.White, LuxuryChampagneGold)
                                    )
                                ),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            if (timerState == ProTimerState.RUNNING) {
                                Spacer(modifier = Modifier.width(8.dp))
                                LuxuryStatusPill(
                                    text = "LIVE",
                                    accentColor = LuxuryTacticalEmerald,
                                    showDot = true
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PRECISION CHRONOGRAPH • 10 SYNTHESIZED SOUNDS",
                            color = LuxuryPlatinumSubtitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp
                        )
                    }

                    if (showCloseButton) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A2438))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Extra 3: Custom Task Label Input Field
            OutlinedTextField(
                value = timerLabel,
                onValueChange = { ProTimerManager.setTimerLabel(it) },
                placeholder = { Text("Timer Task Label (e.g., Study, Gym, Coding)", color = Color(0xFF64748B)) },
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = LuxuryChampagneGold,
                    unfocusedBorderColor = Color(0xFF2E3D59),
                    focusedContainerColor = Color(0xFF0E1420),
                    unfocusedContainerColor = Color(0xFF0E1420)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // =====================================================================
            // CORE 1 + EXTRA 4: ELEVATED OBSIDIAN CENTER STAGE DOUBLE-RING DIAL
            // =====================================================================
            val dialBorderBrush = when (timerState) {
                ProTimerState.FINISHED_RINGING -> Brush.linearGradient(listOf(LuxuryCrimsonAlert, Color(0xFF991B1B)))
                ProTimerState.RUNNING -> LuxuryBrushes.activeHighlightBorder
                else -> LuxuryBrushes.specularBorder
            }

            LuxuryGlassCard(
                cornerRadius = 22.dp,
                borderBrush = dialBorderBrush,
                borderWidth = if (timerState == ProTimerState.RUNNING || timerState == ProTimerState.FINISHED_RINGING) 1.4.dp else 1.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(246.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Double-Ring Track + Glowing Gradient Progress Canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val outerStroke = 14.dp.toPx()
                            val innerStroke = 2.dp.toPx()
                            val diameter = size.minDimension - outerStroke
                            val topLeft = Offset(outerStroke / 2f, outerStroke / 2f)
                            val arcSize = Size(diameter, diameter)

                            // Outer dark recessed ring track
                            drawArc(
                                color = Color(0xFF0A0E17),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = outerStroke, cap = StrokeCap.Round)
                            )

                            // Subtle metallic inner specular ring track (double-ring depth)
                            val innerInset = outerStroke + 8.dp.toPx()
                            val innerDiameter = (size.minDimension - innerInset).coerceAtLeast(1f)
                            drawArc(
                                color = Color(0xFF2E3D59).copy(alpha = 0.5f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = Offset(innerInset / 2f, innerInset / 2f),
                                size = Size(innerDiameter, innerDiameter),
                                style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                            )

                            // Active Glowing Gradient Progress Arc
                            if (timerState != ProTimerState.FINISHED_RINGING) {
                                val sweepBrush = Brush.sweepGradient(
                                    colors = listOf(
                                        LuxuryChampagneGold,
                                        arcColor,
                                        LuxurySapphireCyan,
                                        LuxuryChampagneGold
                                    )
                                )
                                drawArc(
                                    brush = sweepBrush,
                                    startAngle = -90f,
                                    sweepAngle = 360f * progress,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = outerStroke, cap = StrokeCap.Round)
                                )
                            } else {
                                // Pulsing full crimson ring when ringing
                                drawArc(
                                    color = LuxuryCrimsonAlert.copy(alpha = pulseAlpha),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = outerStroke, cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Digital Readout Content
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (timerState == ProTimerState.FINISHED_RINGING) {
                                // Extra 6: Overtime Counter After Zero
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = LuxuryCrimsonAlert,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .alpha(pulseAlpha)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "TIME IS UP!",
                                    color = LuxuryCrimsonAlert,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = "+${ProTimerManager.formatOvertime(overtimeMs)}",
                                    color = LuxuryChampagneGold,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "OVERTIME ELAPSED",
                                    color = LuxuryPlatinumSubtitle,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            } else {
                                LuxuryStatusPill(
                                    text = when (timerState) {
                                        ProTimerState.RUNNING -> "RUNNING"
                                        ProTimerState.PAUSED -> "PAUSED"
                                        else -> "READY"
                                    },
                                    accentColor = when (timerState) {
                                        ProTimerState.RUNNING -> LuxuryTacticalEmerald
                                        ProTimerState.PAUSED -> LuxuryChampagneGold
                                        else -> LuxurySapphireCyan
                                    }
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Extra 4: HH:MM:SS.d Digits Readout
                                val totalSec = (remainingMs / 1000L).coerceAtLeast(0L)
                                val hours = totalSec / 3600L
                                val mins = (totalSec % 3600L) / 60L
                                val secs = totalSec % 60L
                                val deci = ((remainingMs % 1000L) / 100L).coerceIn(0L, 9L)

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = if (hours > 0) {
                                            String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, mins, secs)
                                        } else {
                                            String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
                                        },
                                        color = Color.White,
                                        fontSize = if (hours > 0) 30.sp else 38.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = ".$deci",
                                        color = arcColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                if (isLoopEnabled) {
                                    Text(
                                        text = "🔁 Cycle #${completedCycles + 1}",
                                        color = LuxuryChampagneGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "TARGET • ${ProTimerManager.formatTime(totalDurationMs)}",
                                        color = LuxuryPlatinumSubtitle,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

                Spacer(modifier = Modifier.height(18.dp))

                // =====================================================================
                // IDLE DURATION STEPPERS (Hours / Minutes / Seconds)
                // =====================================================================
                if (timerState == ProTimerState.IDLE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hours Stepper
                        DurationStepperCard(
                            label = "HOURS",
                            value = inputHours,
                            onMinus = {
                                if (inputHours > 0) {
                                    inputHours -= 1
                                    ProTimerManager.setDuration(inputHours, inputMinutes, inputSeconds)
                                }
                            },
                            onPlus = {
                                if (inputHours < 23) {
                                    inputHours += 1
                                    ProTimerManager.setDuration(inputHours, inputMinutes, inputSeconds)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Minutes Stepper
                        DurationStepperCard(
                            label = "MINUTES",
                            value = inputMinutes,
                            onMinus = {
                                if (inputMinutes > 0) {
                                    inputMinutes -= 1
                                    ProTimerManager.setDuration(inputHours, inputMinutes, inputSeconds)
                                }
                            },
                            onPlus = {
                                if (inputMinutes < 59) {
                                    inputMinutes += 1
                                    ProTimerManager.setDuration(inputHours, inputMinutes, inputSeconds)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Seconds Stepper
                        DurationStepperCard(
                            label = "SECONDS",
                            value = inputSeconds,
                            onMinus = {
                                if (inputSeconds > 0) {
                                    inputSeconds -= 1
                                    ProTimerManager.setDuration(inputHours, inputMinutes, inputSeconds)
                                }
                            },
                            onPlus = {
                                if (inputSeconds < 59) {
                                    inputSeconds += 1
                                    ProTimerManager.setDuration(inputHours, inputMinutes, inputSeconds)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // =====================================================================
                // EXTRA 1: QUICK-ADD CHIPS (+30s, +1m, +5m, +10m, +30m)
                // =====================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickAdds = listOf(
                        "+30s" to 30L,
                        "+1m" to 60L,
                        "+5m" to 300L,
                        "+10m" to 600L,
                        "+30m" to 1800L
                    )
                    quickAdds.forEach { (label, secs) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(LuxuryBrushes.cardSurface)
                                .border(
                                    1.dp,
                                    LuxurySapphireCyan.copy(alpha = 0.35f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { ProTimerManager.addSeconds(secs) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = label,
                                color = LuxurySapphireCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // =====================================================================
                // EXTRA 2: SMART PRESETS CHIPS + SAVE CUSTOM
                // =====================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    customPresets.forEach { (name, ms) ->
                        val isSelected = totalDurationMs == ms
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) LuxuryChampagneGold.copy(alpha = 0.16f)
                                    else Color(0xFF101725)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) LuxuryChampagneGold
                                    else LuxurySapphireCyan.copy(alpha = 0.22f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { ProTimerManager.applyPreset(ms) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = name,
                                color = if (isSelected) LuxuryChampagneGold else LuxuryPlatinumSubtitle,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            )
                        }
                    }

                    // Save Current Preset Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(LuxuryTacticalEmerald.copy(alpha = 0.14f))
                            .border(
                                1.dp,
                                LuxuryTacticalEmerald.copy(alpha = 0.55f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                val label = timerLabel.ifBlank { "Custom" }
                                val formatted = ProTimerManager.formatTime(totalDurationMs)
                                ProTimerManager.saveCurrentAsPreset("⭐ $label ($formatted)")
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "➕ Save Current",
                            color = LuxuryTacticalEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // =====================================================================
                // CORE 2: ACTION BUTTONS (Start / Pause / Resume / Reset / Stop)
                // =====================================================================
                when (timerState) {
                    ProTimerState.IDLE -> {
                        Button(
                            onClick = { ProTimerManager.start(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .jellyElastic(baseCornerRadius = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuxuryTacticalEmerald,
                                contentColor = Color(0xFF040810)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "▶️ START TIMER",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    ProTimerState.RUNNING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Pause Button
                            Button(
                                onClick = { ProTimerManager.pause() },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(50.dp)
                                    .jellyElastic(baseCornerRadius = 14.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LuxuryChampagneGold,
                                    contentColor = Color(0xFF06080E)
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PAUSE", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }

                            // Extra 7: Lap / Split Button
                            Button(
                                onClick = { ProTimerManager.addLap() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .jellyElastic(baseCornerRadius = 14.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0D1729),
                                    contentColor = LuxurySapphireCyan
                                ),
                                border = BorderStroke(1.dp, LuxurySapphireCyan.copy(alpha = 0.55f))
                            ) {
                                Icon(imageVector = Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LAP", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }

                            // Reset Button
                            Button(
                                onClick = { ProTimerManager.reset() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .jellyElastic(baseCornerRadius = 14.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF141824),
                                    contentColor = LuxuryCrimsonAlert
                                ),
                                border = BorderStroke(1.dp, LuxuryCrimsonAlert.copy(alpha = 0.45f))
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RESET", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }
                    }

                    ProTimerState.PAUSED -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Resume Button
                            Button(
                                onClick = { ProTimerManager.resume(context) },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp)
                                    .jellyElastic(baseCornerRadius = 14.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LuxuryTacticalEmerald,
                                    contentColor = Color(0xFF040810)
                                )
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("RESUME", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }

                            // Reset Button
                            Button(
                                onClick = { ProTimerManager.reset() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .jellyElastic(baseCornerRadius = 14.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF141824),
                                    contentColor = LuxuryCrimsonAlert
                                ),
                                border = BorderStroke(1.dp, LuxuryCrimsonAlert.copy(alpha = 0.45f))
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RESET", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }
                    }

                    ProTimerState.FINISHED_RINGING -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Giant Stop Sound & Reset Button
                            Button(
                                onClick = { ProTimerManager.stopAndReset() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .jellyElastic(baseCornerRadius = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LuxuryCrimsonAlert,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "⏹️ STOP SOUND & RESET TIMER",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Restart Same Timer Button
                            Button(
                                onClick = { ProTimerManager.restartSameTimer(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .jellyElastic(baseCornerRadius = 14.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0D1729),
                                    contentColor = LuxurySapphireCyan
                                ),
                                border = BorderStroke(1.dp, LuxurySapphireCyan.copy(alpha = 0.55f))
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🔁 RESTART SAME TIMER (${ProTimerManager.formatTime(totalDurationMs)})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =====================================================================
                // TOGGLES ROW (Extra 5 Auto-Loop, Extra 8 10s Beep, Extra 9 Keep Screen)
                // =====================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Extra 5: Auto-Loop Toggle
                    ToggleFilterChip(
                        label = if (isLoopEnabled) "🔁 Auto-Loop: ON ($completedCycles)" else "🔁 Auto-Loop: OFF",
                        isActive = isLoopEnabled,
                        activeColor = LuxurySapphireCyan,
                        onClick = { ProTimerManager.toggleLoop() }
                    )

                    // Extra 8: Final 10s Tick Beep Toggle
                    ToggleFilterChip(
                        label = if (isFinalTenSecBeepEnabled) "🔟 Last 10s Beep: ON" else "🔟 Last 10s Beep: OFF",
                        isActive = isFinalTenSecBeepEnabled,
                        activeColor = LuxuryChampagneGold,
                        onClick = { ProTimerManager.toggleFinalTenSecBeep() }
                    )

                    // Extra 9: Keep Screen Awake Toggle
                    ToggleFilterChip(
                        label = if (isKeepScreenOnEnabled) "💡 Keep Screen ON" else "💡 Keep Screen: System",
                        isActive = isKeepScreenOnEnabled,
                        activeColor = LuxuryTacticalEmerald,
                        onClick = { ProTimerManager.toggleKeepScreenOn() }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // =====================================================================
                // CORE 3: 5 SOUND TOPICS & 10 SYNTHESIZED SOUNDS WITH PREVIEW
                // =====================================================================
                LuxuryGlassCard(cornerRadius = 22.dp) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎵 TIMER COMPLETION SOUNDS",
                                color = LuxuryChampagneGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• 5 Topics / 10 Synths",
                                color = LuxuryPlatinumSubtitle,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Row 1: 5 Topic Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProTimerManager.TOPICS.forEach { topic ->
                                val isSelected = selectedTopic == topic
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) LuxuryChampagneGold.copy(alpha = 0.16f)
                                            else Color(0xFF0E1523)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) LuxuryChampagneGold
                                            else LuxurySapphireCyan.copy(alpha = 0.2f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { ProTimerManager.setTopic(topic) }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = topic,
                                        color = if (isSelected) LuxuryChampagneGold else LuxuryPlatinumSubtitle,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Row 2: Sounds in the Selected Topic
                        val soundsInTopic = ProTimerManager.SOUNDS.filter { it.topic == selectedTopic }
                        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            soundsInTopic.forEach { sound ->
                                val isSelected = selectedSoundId == sound.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) LuxurySapphireCyan.copy(alpha = 0.11f)
                                            else Color(0xFF0D1422)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) LuxuryBrushes.activeHighlightBorder
                                            else LuxuryBrushes.specularBorder,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            ProTimerManager.setSelectedSoundId(sound.id)
                                            ProTimerManager.previewSound(sound.id)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isSelected) "🔘 " else "⚪ ",
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = sound.name,
                                                color = if (isSelected) LuxuryChampagneGold else Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = sound.description,
                                            color = LuxuryPlatinumSubtitle,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(start = 18.dp, top = 2.dp)
                                        )
                                    }

                                    // Preview Button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) LuxurySapphireCyan
                                                else Color(0xFF162235)
                                            )
                                            .clickable {
                                                ProTimerManager.setSelectedSoundId(sound.id)
                                                ProTimerManager.previewSound(sound.id)
                                            }
                                            .padding(horizontal = 11.dp, vertical = 7.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = null,
                                                tint = if (isSelected) Color(0xFF040810) else LuxurySapphireCyan,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Preview",
                                                color = if (isSelected) Color(0xFF040810) else LuxurySapphireCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =====================================================================
                // EXTRA 7: RECORDED LAPS / SPLIT TIMESTAMPS
                // =====================================================================
                if (laps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(18.dp))
                    LuxuryGlassCard(cornerRadius = 20.dp) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏁 RECORDED LAPS & SPLITS (${laps.size})",
                                    color = LuxurySapphireCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )

                                Text(
                                    text = "Clear All",
                                    color = LuxuryCrimsonAlert,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { ProTimerManager.clearLaps() }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            laps.take(6).forEach { lapText ->
                                Text(
                                    text = lapText,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DurationStepperCard(
    label: String,
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier
) {
    LuxuryGlassCard(
        modifier = modifier,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = LuxuryPlatinumSubtitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = String.format(java.util.Locale.US, "%02d", value),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onMinus,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0E1626))
                        .border(1.dp, LuxurySapphireCyan.copy(alpha = 0.28f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Minus",
                        tint = LuxurySapphireCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onPlus,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0E1626))
                        .border(1.dp, LuxuryChampagneGold.copy(alpha = 0.35f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Plus",
                        tint = LuxuryChampagneGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleFilterChip(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) activeColor.copy(alpha = 0.16f)
                else Color(0xFF0D1422)
            )
            .border(
                1.dp,
                if (isActive) activeColor else Color(0xFF233046),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (isActive) activeColor else LuxuryPlatinumSubtitle,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold
        )
    }
}
