package com.leetcodeautomation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/**
 * "Neon Terminal" design system — see /neon_terminal/DESIGN.md from the Stitch export this
 * was translated from. Hanken Grotesk / JetBrains Mono are approximated with the platform
 * default and monospace font families; swap in the real font files under res/font to match
 * exactly.
 */

// Canvas / surfaces
val CanvasBlack = Color(0xFF0D1117)
val SurfaceCard = Color(0xFF161B22)
val SurfaceContainerLowest = Color(0xFF0A0E14)
val SurfaceContainerLow = Color(0xFF181C22)
val SurfaceOverlay = Color(0xFF21262D)
val OutlineVariant = Color(0xFF30363D)
val TerminalBlack = Color(0xFF000000)

// Text
val OnSurface = Color(0xFFDFE2EB)
val OnSurfaceVariant = Color(0xFFC1CAB1)

// Brand
val NvidiaGreen = Color(0xFF76B900)
val NvidiaGreenBright = Color(0xFF94DA32)
val LeetCodeOrange = Color(0xFFFFA116)
val SecondaryContainer = Color(0xFFF29600)
val ErrorRed = Color(0xFFFFB4AB)
val AcceptedGreen = NvidiaGreen

val HankenGrotesk = FontFamily.Default
val JetBrainsMono = FontFamily.Monospace

private val DarkColors = darkColorScheme(
    primary = NvidiaGreenBright,
    onPrimary = Color(0xFF203700),
    secondary = LeetCodeOrange,
    background = CanvasBlack,
    surface = SurfaceCard,
    surfaceVariant = SurfaceOverlay,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorRed,
    outline = OutlineVariant,
)

@Composable
fun LeetCodeAutomationTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
