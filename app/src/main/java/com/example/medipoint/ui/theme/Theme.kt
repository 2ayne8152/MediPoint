package com.example.medipoint.ui.theme

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

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00001A),          // Primary button color
    onPrimary = Color.White,               // Text on primary
    secondary = Color(0xFF0A0A1A),        // Accent
    onSecondary = Color.White,
    tertiary = Color(0xFFFF4081),         // Accent color
    background = Color(0xFFF8F8FF),       // Background for screens
    onBackground = Color(0xFF0A0A1A),     // Text on background
    surface = Color.White,                 // Cards, etc.
    onSurface = Color(0xFF0A0A1A),
    surfaceVariant = Color(0xFFE0E0E0),   // Subtle surface
    onSurfaceVariant = Color.Gray,
    outline = Color.LightGray              // Borders, outlines
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0A0A1A),
    onPrimary = Color.White,
    secondary = Color(0xFF1A1A2E),
    onSecondary = Color.White,
    tertiary = Color(0xFFFF4081),         // Accent color
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1A1A2E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C3E),
    onSurfaceVariant = Color.Gray,
    outline = Color.Gray
)

@Composable
fun MediPointTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
