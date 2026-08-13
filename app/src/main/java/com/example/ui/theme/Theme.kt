package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFEF5350),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFB71C1C),
    onPrimaryContainer = Color(0xFFFFEBEE),
    secondary = Color(0xFFE57373),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonRedPrimary,
    onPrimary = Color.White,
    primaryContainer = RedContainer,
    onPrimaryContainer = OnRedContainer,
    secondary = CrimsonRedSecondary,
    onSecondary = Color.White,
    background = SurfaceLight,
    surface = Color.White,
    surfaceVariant = SurfaceVariantLight,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F)
)

@Composable
fun RubaiyaTelecomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
