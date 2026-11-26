package com.invex.one.template.core.designsystem.molecule

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize

/** `Default`: radius 12, 32dp icon circle, label caption. `Compact`: radius 18, 28dp icon square, body caption. */
enum class StatTileStyle { Default, Compact }

/** KPI tile: tinted icon, big value and caption, centered. */
@Composable
fun StatTile(
    @DrawableRes icon: Int,
    iconContainerColor: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    style: StatTileStyle = StatTileStyle.Default,
) {
    val colors = AppTheme.colors
    val compact = style == StatTileStyle.Compact
    val shape = if (compact) AppTheme.shapes.mediumLarge else AppTheme.shapes.small
    Column(
        modifier = modifier
            .appShadow(AppTheme.elevation.xs, shape, colors.shadow)
            .background(colors.surfaceContainerLowest, shape)
            .padding(if (compact) AppTheme.spacing.sm else AppTheme.spacing.md)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .padding(bottom = if (compact) AppTheme.spacing.xxs else 0.dp)
                .size(if (compact) AppSize.iconSquare else AppSize.statTileIcon)
                .background(iconContainerColor, if (compact) AppTheme.shapes.extraSmall else AppTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) { AppIcon(icon) }
        Text(value, style = AppTheme.typography.numeric, color = colors.onSurface, textAlign = TextAlign.Center, maxLines = 1)
        Text(
            label,
            style = if (compact) AppTheme.typography.bodySmall else AppTheme.typography.label,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun StatTilePreview() {
    AppTheme {
        Row(Modifier.padding(AppTheme.spacing.lg), horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            StatTile(AppIcons.TrophyFilled, AppTheme.colors.tertiaryFixed, "4.096", "Récord global", Modifier.weight(1f))
            StatTile(AppIcons.GamepadFilled, AppTheme.colors.secondaryFixed, "142", "Partidas", Modifier.weight(1f))
        }
    }
}
