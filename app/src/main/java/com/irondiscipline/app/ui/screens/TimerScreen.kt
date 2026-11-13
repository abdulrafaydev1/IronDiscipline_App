package com.irondiscipline.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.irondiscipline.app.timer.ProTimerContent

/**
 * Dedicated full-screen Pro Countdown Timer tab screen matching the Obsidian Dark-Luxury theme.
 */
@Composable
fun TimerScreen(
    modifier: Modifier = Modifier
) {
    ProTimerContent(
        showCloseButton = false,
        modifier = modifier
    )
}
