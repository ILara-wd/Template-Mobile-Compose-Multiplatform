package com.invex.one.template.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.invex.one.template.core.designsystem.token.AppColors
import com.invex.one.template.core.designsystem.token.AppElevation
import com.invex.one.template.core.designsystem.token.AppShapes
import com.invex.one.template.core.designsystem.token.AppSpacing
import com.invex.one.template.core.designsystem.token.AppTypography
import com.invex.one.template.core.designsystem.token.LightAppColors

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }
private val LocalAppTypography = staticCompositionLocalOf { AppTypography() }
private val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }
private val LocalAppShapes = staticCompositionLocalOf { AppShapes() }
private val LocalAppElevation = staticCompositionLocalOf { AppElevation() }

/** Entry point to the design tokens: `AppTheme.colors.primary`, `AppTheme.spacing.lg`… */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
    val typography: AppTypography
        @Composable @ReadOnlyComposable get() = LocalAppTypography.current
    val spacing: AppSpacing
        @Composable @ReadOnlyComposable get() = LocalAppSpacing.current
    val shapes: AppShapes
        @Composable @ReadOnlyComposable get() = LocalAppShapes.current
    val elevation: AppElevation
        @Composable @ReadOnlyComposable get() = LocalAppElevation.current
}

/**
 * Provides the design tokens and a matching Material 3 theme, so stock Material
 * components (ripples, text selection, dialogs…) pick up the same palette.
 */
@Composable
fun AppTheme(
    colors: AppColors = LightAppColors,
    content: @Composable () -> Unit,
) {
    val typography = AppTypography()
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides typography,
        LocalAppSpacing provides AppSpacing(),
        LocalAppShapes provides AppShapes(),
        LocalAppElevation provides AppElevation(),
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            content = content,
        )
    }
}

private fun AppColors.toMaterialColorScheme() = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    secondaryContainer = secondaryContainer,
    tertiary = tertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    error = error,
    errorContainer = errorContainer,
    inverseSurface = inverseSurface,
    inverseOnSurface = inverseOnSurface,
    onErrorContainer = onErrorContainer,
    background = background,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    outlineVariant = outlineVariant,
    surfaceContainerLowest = surfaceContainerLowest,
    surfaceContainerLow = surfaceContainerLow,
    surfaceContainer = surfaceContainer,
    surfaceContainerHigh = surfaceContainerHigh,
    surfaceContainerHighest = surfaceContainerHighest,
)
