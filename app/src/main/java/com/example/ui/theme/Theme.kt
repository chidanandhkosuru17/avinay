package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SafePathDarkColorScheme = darkColorScheme(
    primary = SafeSaffron,
    onPrimary = Color.Black,
    primaryContainer = SafeSaffronDark,
    onPrimaryContainer = Color.White,
    secondary = SafeTeal,
    onSecondary = Color.Black,
    secondaryContainer = MidnightSurfaceVariant,
    onSecondaryContainer = SafeTeal,
    tertiary = SafeGreen,
    onTertiary = Color.Black,
    background = MidnightDark,
    onBackground = Color(0xFFF1F5F9),
    surface = MidnightSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = NightBorder
)

private val SafePathLightColorScheme = lightColorScheme(
    primary = SafePathLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = SafeSaffronDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF7C2D12),
    tertiary = SafeTeal,
    onTertiary = Color.White,
    background = SafePathLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = SafePathLightCard,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SafePathDarkColorScheme else SafePathLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
