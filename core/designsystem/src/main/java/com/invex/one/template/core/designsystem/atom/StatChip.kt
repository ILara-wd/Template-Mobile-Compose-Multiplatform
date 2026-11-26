package com.invex.one.template.core.designsystem.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppAlpha

enum class StatChipSize { Regular, Compact }

/**
 * Pill with an icon and a short label: streaks, level, coins, quick stats.
 *
 * `Regular` (12×6, gap 6, soft shadow) is used in content; `Compact` (10×4, gap 4) in the top bar.
 */
@Composable
fun StatChip(
    @DrawableRes icon: Int,
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    size: StatChipSize = StatChipSize.Regular,
    contentColor: Color = AppTheme.colors.onSurface,
) {
    val spacing = AppTheme.spacing
    val shape = AppTheme.shapes.pill
    val (padding, gap, elevation) = when (size) {
        StatChipSize.Regular -> Triple(PaddingValues(spacing.md, spacing.xs), spacing.xs, AppTheme.elevation.xs)
        StatChipSize.Compact -> Triple(PaddingValues(spacing.smd, spacing.xxs), spacing.xxs, AppTheme.elevation.none)
    }
    // Android shadows show through translucent fills, so tinted chips are composited
    // over the surface first (same visual result as the Figma rgba fill on the page background).
    val background = if (elevation.value > 0f) containerColor.compositeOver(AppTheme.colors.surface) else containerColor
    Row(
        modifier = modifier
            .chipShadow(elevation, shape)
            .background(background, shape)
            .padding(padding),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(icon)
        Text(text = text, style = AppTheme.typography.label, color = contentColor)
    }
}

@Composable
private fun Modifier.chipShadow(elevation: Dp, shape: Shape): Modifier =
    if (elevation.value > 0f) appShadow(elevation, shape, AppTheme.colors.shadow) else this

@Preview
@Composable
private fun StatChipPreview() {
    AppTheme {
        Row(Modifier.padding(AppTheme.spacing.lg), horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            StatChip(AppIcons.Flame, "Racha: 5 días", AppTheme.colors.tertiaryContainer.copy(alpha = AppAlpha.Tint))
            StatChip(AppIcons.Coin, "1.420", AppTheme.colors.surfaceContainerHigh, size = StatChipSize.Compact)
        }
    }
}
