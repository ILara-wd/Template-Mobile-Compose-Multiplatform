package com.invex.one.template.feature.games.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.invex.one.template.core.designsystem.atom.ButtonVariant
import com.invex.one.template.core.designsystem.atom.CategoryTag
import com.invex.one.template.core.designsystem.atom.IconTile
import com.invex.one.template.core.designsystem.atom.MonogramTile
import com.invex.one.template.core.designsystem.atom.PrimaryButton
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize
import com.invex.one.template.feature.games.R
import com.invex.one.template.feature.games.model.Game

/** Catalog card: artwork tile + category tag, name, two-line description and play button. */
@Composable
internal fun GameCatalogCard(
    game: Game,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val type = AppTheme.typography
    val shape = AppTheme.shapes.small
    val style = game.category.style()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .appShadow(AppTheme.elevation.md, shape, colors.shadow)
            .background(colors.surfaceContainerLowest, shape)
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            val artwork = game.artwork()
            if (artwork != null) {
                IconTile(icon = artwork, containerColor = style.tile, size = AppSize.iconTileLarge)
            } else {
                MonogramTile(game.monogram(), style.tile, style.tagContent, size = AppSize.iconTileLarge)
            }
            CategoryTag(
                text = stringResource(style.label),
                containerColor = style.tagContainer,
                contentColor = style.tagContent,
            )
        }
        Column {
            Text(game.name, style = type.subtitle, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            // Reserve two lines so every card in a row has the same height.
            Text(
                game.description,
                style = type.bodySmall,
                color = colors.onSurfaceVariant,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        PrimaryButton(
            text = stringResource(R.string.games_play),
            onClick = onPlay,
            variant = ButtonVariant.Accent,
            leadingIcon = AppIcons.PlayFilled,
        )
    }
}
