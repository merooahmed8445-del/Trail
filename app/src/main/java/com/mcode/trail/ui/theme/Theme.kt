package com.mcode.trail.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = TrailPrimary,
    onPrimary = TrailTextOnDark,
    primaryContainer = TrailPrimaryLight,
    onPrimaryContainer = TrailTextPrimary,

    secondary = TrailSecondary,
    onSecondary = TrailTextPrimary,
    secondaryContainer = TrailSecondaryDark,
    onSecondaryContainer = TrailTextOnDark,

    tertiary = TrailTertiary,

    background = TrailBackground,
    onBackground = TrailTextPrimary,

    surface = TrailSurface,
    onSurface = TrailTextPrimary,

    surfaceVariant = TrailSurfaceVariant,
    onSurfaceVariant = TrailTextSecondary,

    outline = TrailTextSecondary
)

private val DarkColors = darkColorScheme(
    primary = TrailPrimary,
    onPrimary = TrailTextOnDark,
    primaryContainer = TrailPrimaryDark,
    onPrimaryContainer = TrailTextOnDark,

    secondary = TrailSecondary,
    onSecondary = TrailTextPrimary,
    secondaryContainer = TrailSecondaryDark,
    onSecondaryContainer = TrailTextOnDark,

    tertiary = TrailTertiary,

    background = TrailBackgroundDark,
    onBackground = TrailTextOnDark,

    surface = TrailSurfaceDark,
    onSurface = TrailTextOnDark,

    surfaceVariant = TrailSurfaceVariantDark,
    onSurfaceVariant = TrailTextSecondaryOnDark,

    outline = TrailTextSecondaryOnDark
)

@Composable
fun TrailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}