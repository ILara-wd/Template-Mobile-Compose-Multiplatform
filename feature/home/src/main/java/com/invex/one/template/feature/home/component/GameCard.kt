package com.invex.one.template.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.atom.CategoryTag
import com.invex.one.template.core.designsystem.atom.PrimaryButton
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.ValueRow
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.feature.home.R
import com.invex.one.template.feature.home.model.RecentGame

/** Card width in the "Juegos más usados" carousel (Figma: 240). */
internal val GameCardWidth = 240.dp

/** Quick-resume card: category, name, play count, best score and a play action. */
@Composable
internal fun GameCard(
    game: RecentGame,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val type = AppTheme.typography
    val shape = AppTheme.shapes.large
    val category = game.category.style()
    Column(
        modifier = modifier
            .width(GameCardWidth)
            .appShadow(AppTheme.elevation.md, shape, colors.shadow)
            .background(colors.surfaceContainerLowest, shape)
            .padding(spacing.lg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryTag(
                text = stringResource(category.label),
                containerColor = category.container,
                contentColor = category.content,
            )
            AppIcon(category.icon)
        }
        Text(
            game.name,
            style = type.title,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = spacing.xxs),
        )
        Text(
            pluralStringResource(R.plurals.home_games_played, game.timesPlayed, game.timesPlayed),
            style = type.bodySmall,
            color = colors.onSurfaceVariant,
        )
        ValueRow(
            label = stringResource(R.string.home_record),
            value = stringResource(R.string.home_points, formatScore(game.bestScore)),
            suffix = game.bestScoreMultiplier?.let { stringResource(R.string.home_multiplier, it) },
            modifier = Modifier.padding(top = spacing.md),
        )
        PrimaryButton(
            text = stringResource(R.string.home_play),
            onClick = onPlay,
            trailingIcon = AppIcons.Play,
            modifier = Modifier.padding(top = spacing.lg),
        )
    }
}
