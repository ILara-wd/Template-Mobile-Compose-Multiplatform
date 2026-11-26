package com.invex.one.template.feature.records.component

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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.invex.one.template.core.designsystem.atom.CategoryTag
import com.invex.one.template.core.designsystem.atom.RankBadge
import com.invex.one.template.core.designsystem.atom.TagSize
import com.invex.one.template.core.designsystem.format.formatScore
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.feature.records.R
import com.invex.one.template.feature.records.model.GameRecord
import com.invex.one.template.feature.records.model.RecordUnit

/** One personal best: rank badge, game + category + detail, value + unit. Podium rows are highlighted. */
@Composable
internal fun RecordListItem(
    record: GameRecord,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val type = AppTheme.typography
    val shape = AppTheme.shapes.small
    val podium = podiumStyle(record.rank)
    val category = record.category.colors()
    val rank = stringResource(R.string.records_rank, record.rank)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .appShadow(AppTheme.elevation.xs, shape, colors.shadow)
            .background(if (podium != null) colors.surfaceContainerLowest else colors.surfaceContainerLow, shape)
            .padding(spacing.lg)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (podium != null) {
            RankBadge(rank, podium.badgeContainer, podium.badgeContent, icon = podium.medal)
        } else {
            RankBadge(rank, colors.surfaceContainer, colors.onSurfaceVariant)
        }
        Column(Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    record.gameName,
                    style = type.subtitle,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                CategoryTag(
                    text = stringResource(record.category.label()),
                    containerColor = category.container,
                    contentColor = category.content,
                    size = TagSize.Small,
                )
            }
            Text(
                record.detail,
                style = type.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (record.unit == RecordUnit.Round) {
                    stringResource(R.string.records_round_value, record.value)
                } else {
                    formatScore(record.value)
                },
                style = type.numeric,
                color = if (record.rank == 1) colors.primary else colors.onSurface,
                maxLines = 1,
            )
            Text(stringResource(record.unit.label()), style = type.label, color = colors.onSurfaceVariant)
        }
    }
}
