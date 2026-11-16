package com.irondiscipline.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irondiscipline.app.service.ScreenShareSessionManager
import com.irondiscipline.app.ui.theme.LuxuryBgBottom
import com.irondiscipline.app.ui.theme.LuxuryBgMid
import com.irondiscipline.app.ui.theme.LuxuryBorderEnd
import com.irondiscipline.app.ui.theme.LuxuryCardBottom
import com.irondiscipline.app.ui.theme.LuxuryCardTop
import com.irondiscipline.app.ui.theme.LuxuryChampagneGold
import com.irondiscipline.app.ui.theme.LuxuryCrimsonAlert
import com.irondiscipline.app.ui.theme.LuxuryPlatinumSubtitle
import com.irondiscipline.app.ui.theme.LuxurySapphireCyan
import com.irondiscipline.app.ui.theme.LuxuryTacticalEmerald

/**
 * Standalone full-screen navigation targets opened from the Slide-Out Command Sidebar.
 */
enum class SidebarScreenRoute {
    NONE,
    UNIVERSAL_APP_LOCKER,
    LIVE_SCREEN_SHARE,
    STRICT_PROTOCOL_CONTROLS
}

/**
 * Animated left-to-right Obsidian Titanium & Champagne Gold Slide-Out Command Sidebar.
 */
@Composable
fun CommandSidebarDrawerOverlay(
    visible: Boolean,
    isMasterPasswordEnabled: Boolean,
    lockedAppsCount: Int,
    isTestMode: Boolean,
    isAnyAppLocked: Boolean,
    onClose: () -> Unit,
    onSelectRoute: (SidebarScreenRoute) -> Unit
) {
    val isSenderActive by ScreenShareSessionManager.isSenderActive.collectAsState()
    val senderRoomCode by ScreenShareSessionManager.senderRoomCode.collectAsState()
    val viewerSessions by ScreenShareSessionManager.viewerSessions.collectAsState()

    val isLiveShareActive = isSenderActive || viewerSessions.isNotEmpty()
    val shareStatusText = when {
        isSenderActive -> "🟢 SHARING LIVE (#$senderRoomCode)"
        viewerSessions.isNotEmpty() -> "🟢 WATCHING ${viewerSessions.size} PHONE(S)"
        else -> "🛡️ AES-256 READY"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Darkened Backdrop Scrim
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(260)),
            exit = fadeOut(animationSpec = tween(240))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose
                    )
            )
        }

        // Slide-Out Drawer Panel
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(260)
            )
        ) {
            val drawerShape = RoundedCornerShape(topEnd = 26.dp, bottomEnd = 26.dp)
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .clip(drawerShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(LuxuryCardTop, LuxuryBgMid, LuxuryBgBottom)
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                LuxuryChampagneGold.copy(alpha = 0.65f),
                                LuxurySapphireCyan.copy(alpha = 0.45f),
                                LuxuryBorderEnd
                            )
                        ),
                        shape = drawerShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Sidebar Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(LuxuryCardTop, LuxuryCardBottom)
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    brush = LuxuryBrushes.activeHighlightBorder,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Iron Discipline Crest",
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
                                fontSize = 15.sp,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "COMMAND SIDEBAR",
                                color = LuxurySapphireCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    // Circular Close Button ("✕")
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(LuxuryCardTop, LuxuryCardBottom)
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = LuxuryChampagneGold.copy(alpha = 0.65f),
                                shape = CircleShape
                            )
                            .jellyElastic(baseCornerRadius = 19.dp)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Sidebar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Specular Metallic Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    LuxuryChampagneGold.copy(alpha = 0.55f),
                                    LuxurySapphireCyan.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Text(
                    text = "DEDICATED COMMAND SUITES",
                    color = LuxuryPlatinumSubtitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                // Item 1: Universal App Locker Vault
                SidebarNavigationCard(
                    title = "🔐 Universal App Locker Vault",
                    subtitle = "Lock IronDiscipline & third-party phone apps with Master Password",
                    statusText = if (isMasterPasswordEnabled) {
                        "🔒 $lockedAppsCount PROTECTED"
                    } else {
                        "SETUP MASTER LOCK"
                    },
                    accentColor = LuxuryChampagneGold,
                    statusAccentColor = if (isMasterPasswordEnabled) LuxuryTacticalEmerald else LuxuryChampagneGold,
                    icon = Icons.Default.Lock,
                    onClick = { onSelectRoute(SidebarScreenRoute.UNIVERSAL_APP_LOCKER) }
                )

                // Item 2: Live Screen & Audio Share
                SidebarNavigationCard(
                    title = "📡 Live Screen & Audio Share",
                    subtitle = "AES-256 encrypted multi-phone screen & audio streaming",
                    statusText = shareStatusText,
                    accentColor = if (isLiveShareActive) LuxuryTacticalEmerald else LuxurySapphireCyan,
                    statusAccentColor = if (isLiveShareActive) LuxuryTacticalEmerald else LuxurySapphireCyan,
                    icon = Icons.Default.Share,
                    showLivePulse = isLiveShareActive,
                    onClick = { onSelectRoute(SidebarScreenRoute.LIVE_SCREEN_SHARE) }
                )

                // Item 3: Strict Protocol & Test Mode
                val strictAccent = when {
                    isAnyAppLocked -> LuxuryCrimsonAlert
                    isTestMode -> LuxuryChampagneGold
                    else -> LuxuryTacticalEmerald
                }
                val strictStatus = when {
                    isAnyAppLocked -> "🔒 5-HR LOCK ACTIVE"
                    isTestMode -> "⚡ 1-MIN TEST MODE"
                    else -> "🛡️ STRICT 30 MINS/APP"
                }
                SidebarNavigationCard(
                    title = "🛡️ Strict Protocol & Test Mode",
                    subtitle = "Strict Protocol: 30 Mins/App • Delete App (Enter PIN) • 1-Min Test Mode",
                    statusText = strictStatus,
                    accentColor = strictAccent,
                    statusAccentColor = strictAccent,
                    icon = Icons.Default.Shield,
                    onClick = { onSelectRoute(SidebarScreenRoute.STRICT_PROTOCOL_CONTROLS) }
                )

                Spacer(modifier = Modifier.height(6.dp))

                LuxuryGlassCard(
                    cornerRadius = 16.dp,
                    borderBrush = LuxuryBrushes.specularBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PERSISTENT ENGINE PROTECTION",
                            color = LuxuryChampagneGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wall-clock penalty locks, active broadcasts, and Master Password guards continue running smoothly in the background.",
                            color = LuxuryPlatinumSubtitle,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarNavigationCard(
    title: String,
    subtitle: String,
    statusText: String,
    accentColor: Color,
    statusAccentColor: Color,
    icon: ImageVector,
    showLivePulse: Boolean = false,
    onClick: () -> Unit
) {
    LuxuryGlassCard(
        cornerRadius = 18.dp,
        borderBrush = Brush.linearGradient(
            colors = listOf(accentColor.copy(alpha = 0.72f), Color(0xFF1A2538))
        ),
        modifier = Modifier
            .jellyElastic(baseCornerRadius = 18.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.14f))
                        .border(1.dp, accentColor.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                LuxuryStatusPill(
                    text = statusText,
                    accentColor = statusAccentColor,
                    showDot = showLivePulse
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = LuxuryPlatinumSubtitle,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
