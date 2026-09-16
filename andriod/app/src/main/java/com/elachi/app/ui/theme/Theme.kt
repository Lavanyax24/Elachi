package com.elachi.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ElachiGreen,
    onPrimary = ElachiSurface,
    secondary = ElachiBrown,
    onSecondary = ElachiSurface,
    background = ElachiCream,
    onBackground = ElachiTextPrimary,
    surface = ElachiSurface,
    onSurface = ElachiTextPrimary,
    surfaceVariant = ElachiBrownLight,
    error = ElachiDanger,
)

private val DarkColors = darkColorScheme(
    primary = ElachiGreen,
    onPrimary = ElachiSurface,
    secondary = ElachiOrange,
    onSecondary = ElachiTextPrimary,
    background = Color(0xFF121212),
    onBackground = ElachiSurface,
    surface = Color(0xFF1E1E1E),
    onSurface = ElachiSurface,
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