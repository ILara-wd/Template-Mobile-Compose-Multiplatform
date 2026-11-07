package com.invex.sportkid.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

/**
 * Tema raíz de SportKid.
 *
 * - El [ColorScheme] M3 cambia según [palette] y [themeMode].
 * - [LocalSportColors] solo cambia entre claro y oscuro, **nunca** con la paleta.
 */
@Composable
fun SportKidTheme(
    palette: AppPalette = AppPalette.Ocean,
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    CompositionLocalProvider(
        LocalSportColors provides if (darkTheme) DarkSportColors else LightSportColors,
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = sportKidColorScheme(palette, darkTheme),
            typography = SportKidTypography,
            shapes = SportKidShapes,
            content = content,
        )
    }
}

@Composable
private fun sportKidColorScheme(palette: AppPalette, darkTheme: Boolean): ColorScheme =
    when (palette) {
        AppPalette.Automatic ->
            if (AppPalette.isDynamicColorSupported) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                oceanScheme(darkTheme)
            }

        AppPalette.Ocean -> oceanScheme(darkTheme)

        else -> rememberDynamicColorScheme(
            seedColor = palette.seed,
            isDark = darkTheme,
            isAmoled = false,
            style = PaletteStyle.TonalSpot,
        )
    }

private fun oceanScheme(darkTheme: Boolean) = if (darkTheme) OceanDarkScheme else OceanLightScheme

/** Acceso corto a los tokens propios: `SportKidTheme.sportColors.tkd`, `SportKidTheme.spacing.lg`. */
object SportKidTheme {
    val sportColors: SportColors
        @Composable @ReadOnlyComposable
        get() = LocalSportColors.current

    val spacing: Spacing
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current
}
