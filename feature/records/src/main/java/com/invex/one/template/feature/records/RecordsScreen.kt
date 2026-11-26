package com.invex.one.template.feature.records

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.one.template.core.designsystem.atom.ChipButton
import com.invex.one.template.core.designsystem.format.formatScore
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.ScreenHeader
import com.invex.one.template.core.designsystem.molecule.StatTile
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.feature.records.component.RecordListItem
import com.invex.one.template.feature.records.data.SampleRecordsData
import com.invex.one.template.feature.records.model.RecordsSummary
import com.invex.one.template.feature.records.model.RecordsUiState

/** Stateful entry point: wires the [RecordsViewModel] to the stateless [RecordsScreen]. */
@Composable
fun RecordsRoute(
    onGlobalRankingClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: RecordsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RecordsScreen(
        state = state,
        onGlobalRankingClick = onGlobalRankingClick,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

@Composable
fun RecordsScreen(
    state: RecordsUiState,
    onGlobalRankingClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val spacing = AppTheme.spacing
    val itemModifier = Modifier.padding(horizontal = spacing.lg)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        item(key = "header") {
            ScreenHeader(
                title = stringResource(R.string.records_title),
                subtitle = stringResource(R.string.records_subtitle),
                action = {
                    ChipButton(
                        text = stringResource(R.string.records_global_ranking),
                        icon = AppIcons.Globe,
                        onClick = onGlobalRankingClick,
                    )
                },
                modifier = itemModifier.padding(top = spacing.lg),
            )
        }
        // Section gap is 16dp: 8dp list spacing + 8dp top padding.
        item(key = "summary") {
            SummaryRow(state.summary, itemModifier.padding(top = spacing.sm))
        }
        item(key = "list_header") {
            Row(
                modifier = itemModifier
                    .padding(top = spacing.sm)
                    .padding(horizontal = spacing.xxs)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.records_list_title),
                    style = AppTheme.typography.subtitle,
                    color = AppTheme.colors.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    pluralStringResource(R.plurals.records_games_count, state.records.size, state.records.size),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.onSurfaceVariant,
                )
            }
        }
        items(state.records, key = { it.id }) { record ->
            RecordListItem(record, itemModifier)
        }
        item(key = "bottom_spacer") { Spacer(Modifier.height(spacing.xl)) }
    }
}

@Composable
private fun SummaryRow(summary: RecordsSummary, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        StatTile(
            icon = AppIcons.TrophyFilled,
            iconContainerColor = colors.tertiaryFixed,
            value = formatScore(summary.bestScore),
            label = stringResource(R.string.records_summary_best),
            modifier = Modifier.weight(1f),
        )
        StatTile(
            icon = AppIcons.GamepadFilled,
            iconContainerColor = colors.secondaryFixed,
            value = formatScore(summary.totalGames),
            label = stringResource(R.string.records_summary_games),
            modifier = Modifier.weight(1f),
        )
        StatTile(
            icon = AppIcons.FlameFilled,
            iconContainerColor = colors.primaryFixed,
            value = pluralStringResource(R.plurals.records_streak_days, summary.streakDays, summary.streakDays),
            label = stringResource(R.string.records_summary_streak),
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(widthDp = 390, heightDp = 1004)
@Composable
private fun RecordsScreenPreview() {
    AppTheme {
        RecordsScreen(state = SampleRecordsData.state, onGlobalRankingClick = {})
    }
}
