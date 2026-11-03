package com.drnitz.youcan.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimarySage,
    onPrimary = DarkBackground,
    primaryContainer = DarkPrimarySageContainer,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkAccentHoney,
    onSecondary = DarkBackground,
    tertiary = DarkAccentTerracotta,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkDivider,
    outlineVariant = DarkSurfaceHighlight
)

private val AmoledColorScheme = DarkColorScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF101211)
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimarySage,
    onPrimary = Color.White,
    primaryContainer = LightPrimarySageContainer,
    onPrimaryContainer = LightPrimarySage,
    secondary = LightAccentHoney,
    onSecondary = Color.White,
    tertiary = LightAccentTerracotta,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightDivider,
    outlineVariant = LightSurfaceHighlight
)

data class ExtendedThemeColors(
    val surfaceHighlight: Color,
    val textTertiary: Color,
    val phaseFocus: Color,
    val phaseShortBreak: Color,
    val phaseLongBreak: Color,
    val isAmoled: Boolean = false
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedThemeColors(
        surfaceHighlight = DarkSurfaceHighlight,
        textTertiary = DarkTextTertiary,
        phaseFocus = PhaseFocusColor,
        phaseShortBreak = PhaseShortBreakColor,
        phaseLongBreak = PhaseLongBreakColor
    )
}

@Composable
fun YoucanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        amoled && darkTheme -> AmoledColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = ExtendedThemeColors(
        surfaceHighlight = if (darkTheme) DarkSurfaceHighlight else LightSurfaceHighlight,
        textTertiary = if (darkTheme) DarkTextTertiary else LightTextTertiary,
        phaseFocus = PhaseFocusColor,
        phaseShortBreak = PhaseShortBreakColor,
        phaseLongBreak = PhaseLongBreakColor,
        isAmoled = amoled && darkTheme
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = YoucanTypography,
            shapes = YoucanShapes,
            content = content
        )
    }
}
