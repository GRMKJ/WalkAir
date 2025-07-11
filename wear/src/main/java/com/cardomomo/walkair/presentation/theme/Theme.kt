package com.cardomomo.walkair.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography

private val DarkColors = ColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    onSurface = Color.White
)

@Composable
fun WalkAirWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography(),
        content = content
    )
}
