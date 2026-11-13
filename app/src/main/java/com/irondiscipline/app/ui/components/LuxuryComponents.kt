package com.irondiscipline.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irondiscipline.app.ui.theme.LuxuryBgBottom
import com.irondiscipline.app.ui.theme.LuxuryBgMid
import com.irondiscipline.app.ui.theme.LuxuryBgTop
import com.irondiscipline.app.ui.theme.LuxuryBorderEnd
import com.irondiscipline.app.ui.theme.LuxuryBorderStart
import com.irondiscipline.app.ui.theme.LuxuryCardBottom
import com.irondiscipline.app.ui.theme.LuxuryCardTop
import com.irondiscipline.app.ui.theme.LuxuryChampagneGold
import com.irondiscipline.app.ui.theme.LuxuryPlatinumSubtitle
import com.irondiscipline.app.ui.theme.LuxurySapphireCyan

/**
 * Shared Obsidian Titanium, Deep Glass & Champagne Gold brushes.
 */
object LuxuryBrushes {
    val primaryBackground: Brush = Brush.verticalGradient(
        colors = listOf(LuxuryBgTop, LuxuryBgMid, LuxuryBgBottom)
    )

    val cardSurface: Brush = Brush.verticalGradient(
        colors = listOf(LuxuryCardTop, LuxuryCardBottom)
    )

    val specularBorder: Brush = Brush.linearGradient(
        colors = listOf(LuxuryBorderStart, LuxuryBorderEnd)
    )

    val activeHighlightBorder: Brush = Brush.linearGradient(
        colors = listOf(LuxuryChampagneGold, LuxurySapphireCyan)
    )

    val sapphireButtonGradient: Brush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
    )
}

/**
 * Elevated Deep Glass card container with gradient fill and 1.dp specular metallic border.
 */
@Composable
fun LuxuryGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    borderBrush: Brush = LuxuryBrushes.specularBorder,
    borderWidth: Dp = 1.dp,
    surfaceBrush: Brush = LuxuryBrushes.cardSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(borderWidth, borderBrush),
                shape = shape
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceBrush),
            content = content
        )
    }
}

/**
 * Executive uppercase section header with optional right-aligned status text.
 */
@Composable
fun LuxurySectionHeader(
    title: String,
    trailingText: String? = null,
    trailingColor: Color = LuxuryChampagneGold,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            color = LuxuryPlatinumSubtitle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp
        )
        if (!trailingText.isNullOrBlank()) {
            Text(
                text = trailingText,
                color = trailingColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
    }
}

/**
 * Glowing pill badge with optional pulsing dot indicator.
 */
@Composable
fun LuxuryStatusPill(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    showDot: Boolean = false,
    dotAlpha: Float = 1f
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.14f))
            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .alpha(dotAlpha)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = accentColor,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}
