package com.elachi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ElachiGreen,
    onPrimary = Color.White,
    secondary = ElachiBrown,
    onSecondary = Color.White,
    background = ElachiCream,
    onBackground = ElachiTextPrimary,
    surface = Color.White,
    onSurface = ElachiTextPrimary,
    surfaceVariant = ElachiBrownLight,
    onSurfaceVariant = ElachiTextSecondary,
    outline = Color(0xFFE5E2DD),
    outlineVariant = Color(0xFFF0EDE9),
    secondaryContainer = Color(0xFFF6F3EE),
    onSecondaryContainer = ElachiGreen,
    error = ElachiDanger,
)

private val DarkColors = darkColorScheme(
    primary = ElachiGreenLight,
    onPrimary = Color.White,
    secondary = ElachiOrange,
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF252820),
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF32362D),
    secondaryContainer = Color(0xFF3D2E13),
    onSecondaryContainer = Color.White,
    error = ElachiDanger,
)

@Composable
fun ElachiTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = ElachiTypography,
        content = content,
    )
}
