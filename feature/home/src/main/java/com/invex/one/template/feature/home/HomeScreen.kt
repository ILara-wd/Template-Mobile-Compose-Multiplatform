package com.invex.one.template.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.one.template.core.designsystem.atom.StatChip
import com.invex.one.template.core.designsystem.atom.TextLinkButton
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.HeadingBlock
import com.invex.one.template.core.designsystem.molecule.SectionHeader
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.feature.home.component.DailyChallengeCard
import com.invex.one.template.feature.home.component.GameCard
import com.invex.one.template.feature.home.component.QuickStatsRow
import com.invex.one.template.feature.home.data.SampleHomeData
import com.invex.one.template.feature.home.model.HomeUiState
import com.invex.one.template.feature.home.model.RecentGame

/** Stateful entry point: wires the [HomeViewModel] to the stateless [HomeScreen]. */
@Composable
fun HomeRoute(
    onPlayGame: (gameId: String, title: String) -> Unit,
    onSeeHistory: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onPlayGame = { onPlayGame(it.id, it.name) },
        onSeeHistory = onSeeHistory,
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onPlayGame: (RecentGame) -> Unit,
    onSeeHistory: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val spacing = AppTheme.spacing
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = contentPadding,
    ) {
        item(key = "challenge") {
            Column(
                modifier = Modifier.padding(start = spacing.lg, end = spacing.lg, top = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HeadingBlock(
                        title = stringResource(R.string.home_pick_challenge_title),
                        subtitle = stringResource(R.string.home_pick_challenge_subtitle),
                    )
                    StatChip(
                        icon = AppIcons.Verified,
                        text = stringResource(R.string.home_level, state.level),
                        containerColor = AppTheme.colors.surfaceContainerHigh,
                    )
                }
                QuickStatsRow(stats = state.quickStats)
                DailyChallengeCard(challenge = state.dailyChallenge)
            }
        }
        item(key = "most_played_header") {
            SectionHeader(
                title = stringResource(R.string.home_most_played),
                leadingIcon = AppIcons.History,
                action = { TextLinkButton(stringResource(R.string.home_see_history), onClick = onSeeHistory) },
                modifier = Modifier.padding(start = spacing.lg, end = spacing.lg, top = spacing.xl, bottom = spacing.sm),
            )
        }
        item(key = "most_played") {
            LazyRow(
                // Figma: the carousel starts flush with the screen edge; vertical padding keeps card shadows visible.
                contentPadding = PaddingValues(end = spacing.lg, top = spacing.xxs, bottom = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
                modifier = Modifier.padding(bottom = spacing.xl),
            ) {
                items(state.mostPlayed, key = { it.id }) { game ->
                    GameCard(game = game, onPlay = { onPlayGame(game) })
                }
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 884)
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            state = SampleHomeData.state,
            onPlayGame = {},
            onSeeHistory = {},
        )
    }
}
