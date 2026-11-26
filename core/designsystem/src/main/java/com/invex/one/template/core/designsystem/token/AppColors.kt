package com.invex.one.template.core.designsystem.token

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Semantic color roles (Material 3 naming, as exported by Figma/Stitch).
 *
 * The Figma file is light-only, so only [LightAppColors] exists. A dark scheme can be
 * added later as a second instance without touching any component.
 */
@Immutable
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val primaryFixed: Color,
    val primaryFixedDim: Color,
    val onPrimaryFixed: Color,
    val onPrimaryFixedVariant: Color,

    val secondary: Color,
    val secondaryContainer: Color,
    val secondaryFixed: Color,
    val secondaryFixedDim: Color,
    val onSecondaryFixed: Color,
    val onSecondaryFixedVariant: Color,

    val tertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val tertiaryFixed: Color,
    val onTertiaryFixed: Color,
    val onTertiaryFixedVariant: Color,

    val errorContainer: Color,
    val onErrorContainer: Color,

    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,

    val shadow: Color,
)

val LightAppColors = AppColors(
    primary = ColorPalette.Green40,
    onPrimary = ColorPalette.White,
    primaryContainer = ColorPalette.Green60,
    onPrimaryContainer = ColorPalette.Green20,
    primaryFixed = ColorPalette.Green80,
    primaryFixedDim = ColorPalette.Green70,
    onPrimaryFixed = ColorPalette.Green10,
    onPrimaryFixedVariant = ColorPalette.Green30,

    secondary = ColorPalette.Blue40,
    secondaryContainer = ColorPalette.Blue60,
    secondaryFixed = ColorPalette.Blue90,
    secondaryFixedDim = ColorPalette.Blue80,
    onSecondaryFixed = ColorPalette.Blue10,
    onSecondaryFixedVariant = ColorPalette.Blue30,

    tertiary = ColorPalette.Orange40,
    tertiaryContainer = ColorPalette.Orange70,
    onTertiaryContainer = ColorPalette.Orange30,
    tertiaryFixed = ColorPalette.Orange90,
    onTertiaryFixed = ColorPalette.Orange10,
    onTertiaryFixedVariant = ColorPalette.Orange28,

    errorContainer = ColorPalette.Red90,
    onErrorContainer = ColorPalette.Red20,

    background = ColorPalette.Indigo98,
    surface = ColorPalette.Indigo98,
    onSurface = ColorPalette.Indigo10,
    onSurfaceVariant = ColorPalette.NeutralVariant30,
    surfaceContainerLowest = ColorPalette.White,
    surfaceContainerLow = ColorPalette.Indigo96,
    surfaceContainer = ColorPalette.Indigo94,
    surfaceContainerHigh = ColorPalette.Indigo92,
    surfaceContainerHighest = ColorPalette.Indigo90,

    shadow = ColorPalette.Black,
)

/** Alpha tokens used for tinted overlays (chips, badges) and translucent bars. */
object AppAlpha {
    /** Tinted chip background, e.g. `tertiaryContainer.copy(alpha = Tint)`. */
    const val Tint = 0.20f
    /** Subtle tinted badge background. */
    const val TintSubtle = 0.15f
    /** Medium tinted tag background. */
    const val TintMedium = 0.25f
    /** Strong tinted tag background. */
    const val TintStrong = 0.30f
    /** Near-opaque overlay over a light container. */
    const val Overlay = 0.70f
    /** Hard "pressable" depth shadow under accent buttons. */
    const val Depth = 0.35f
    /** Avatar ring around the profile picture. */
    const val Ring = 0.40f
    /** Top app bar translucent background. */
    const val BarTop = 0.85f
    /** Bottom navigation translucent background. */
    const val BarBottom = 0.90f
}
