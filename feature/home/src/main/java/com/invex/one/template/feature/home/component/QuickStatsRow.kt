package com.invex.one.template.feature.home.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.invex.one.template.core.designsystem.atom.StatChip
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppAlpha
import com.invex.one.template.feature.home.R
import com.invex.one.template.feature.home.model.QuickStats

/** Horizontally scrollable row of quick player stats. */
@Composable
internal fun QuickStatsRow(
    stats: QuickStats,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            // Leaves room for the chips' soft shadow so it is not clipped by the scroll container.
            .padding(vertical = AppTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatChip(
            icon = AppIcons.Flame,
            text = pluralStringResource(R.plurals.home_streak, stats.streakDays, stats.streakDays),
            containerColor = colors.tertiaryContainer.copy(alpha = AppAlpha.Tint),
        )
        StatChip(
            icon = AppIcons.Bolt,
            text = pluralStringResource(R.plurals.home_total_games, stats.totalGames, formatScore(stats.totalGames)),
            containerColor = colors.secondaryContainer.copy(alpha = AppAlpha.Tint),
        )
        StatChip(
            icon = AppIcons.Medal,
            text = stringResource(R.string.home_weekly_top, stats.weeklyTopPercent),
            containerColor = colors.primaryContainer.copy(alpha = AppAlpha.Tint),
        )
    }
}
