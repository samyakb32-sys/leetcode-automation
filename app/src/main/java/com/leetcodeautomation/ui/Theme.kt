package com.leetcodeautomation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NvidiaGreen = Color(0xFF76B900)
val LeetCodeOrange = Color(0xFFFFA116)
val BackgroundBlack = Color(0xFF0D1117)
val SurfaceDark = Color(0xFF161B22)
val AcceptedGreen = Color(0xFF2EA043)
val ErrorRed = Color(0xFFF85149)

private val DarkColors = darkColorScheme(
    primary = NvidiaGreen,
    secondary = LeetCodeOrange,
    background = BackgroundBlack,
    surface = SurfaceDark,
    error = ErrorRed,
)

@Composable
fun LeetCodeAutomationTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
