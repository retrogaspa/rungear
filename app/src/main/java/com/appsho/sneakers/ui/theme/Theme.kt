package com.appsho.sneakers.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = AccentSoft,
    onPrimaryContainer = AccentDark,
    secondary = Ink,
    onSecondary = Color.White,
    secondaryContainer = CanvasBg,
    onSecondaryContainer = InkSoft,
    background = CanvasBg,
    onBackground = Ink,
    surface = SurfaceWhite,
    onSurface = Ink,
    onSurfaceVariant = InkMuted,
    outline = Border,
    outlineVariant = Border,
    error = Danger,
    onError = Color.White,
    errorContainer = DangerSoft,
    onErrorContainer = Danger
)

@Composable
fun RunGearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
