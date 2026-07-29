package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DevCyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = DevCyanContainer,
    onPrimaryContainer = DevCyanPrimary,
    secondary = DevEmeraldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = DevEmeraldContainer,
    onSecondaryContainer = DevEmeraldSecondary,
    tertiary = DevPurpleTertiary,
    onTertiary = Color.White,
    tertiaryContainer = DevPurpleContainer,
    onTertiaryContainer = DevPurpleTertiary,
    background = DevBackgroundDark,
    onBackground = DevTextPrimaryDark,
    surface = DevSurfaceDark,
    onSurface = DevTextPrimaryDark,
    surfaceVariant = DevCardSurface,
    onSurfaceVariant = DevTextSecondaryDark,
    outline = DevBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006875),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9EEFFE),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF006C35),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8CF8AD),
    onSecondaryContainer = Color(0xFF00210C),
    tertiary = Color(0xFF8B00A8),
    onTertiary = Color.White,
    background = DevBackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = DevSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = DevCardSurfaceLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun DevForgeTheme(
    darkTheme: Boolean = true, // Default to sleek Developer Dark Theme
    dynamicColor: Boolean = false, // Preserve developer brand colors by default
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain legacy name helper for default MainActivity compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    DevForgeTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
