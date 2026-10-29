package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ChessDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1E1B18),
    secondary = AccentBlue,
    onSecondary = Color(0xFF0F172A),
    tertiary = GoldTertiary,
    background = DarkBackground,
    onBackground = LightIvory,
    surface = DarkSurface,
    onSurface = LightIvory,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SlateMuted
)

private val ChessLightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1E1B18),
    secondary = AccentBlue,
    onSecondary = Color.White,
    tertiary = GoldSecondary,
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek tournament dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ChessDarkColorScheme else ChessLightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
