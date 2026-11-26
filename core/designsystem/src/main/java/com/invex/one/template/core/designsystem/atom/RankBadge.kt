package com.invex.one.template.core.designsystem.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize

/**
 * 44dp rank square. With [icon] it is a podium badge (medal + small ordinal, elevated);
 * without it, a plain badge with a large ordinal.
 */
@Composable
fun RankBadge(
    rank: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
) {
    val shape = AppTheme.shapes.small
    val label = AppTheme.typography.label
    Column(
        modifier = modifier
            .size(AppSize.rankBadge)
            .then(if (icon != null) Modifier.appShadow(AppTheme.elevation.xs, shape, AppTheme.colors.shadow) else Modifier)
            .background(containerColor, shape),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            AppIcon(icon)
            // Tight line box (line height = font size) so medal and ordinal stack compactly.
            Text(rank, style = label.copy(lineHeight = label.fontSize), color = contentColor)
        } else {
            Text(rank, style = AppTheme.typography.numeric, color = contentColor)
        }
    }
}

@Preview
@Composable
private fun RankBadgePreview() {
    AppTheme {
        val c = AppTheme.colors
        Row(Modifier.padding(AppTheme.spacing.lg), horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            RankBadge("1º", c.tertiaryContainer, c.onTertiaryContainer, icon = AppIcons.MedalGold)
            RankBadge("4º", c.surfaceContainer, c.onSurfaceVariant)
        }
    }
}
