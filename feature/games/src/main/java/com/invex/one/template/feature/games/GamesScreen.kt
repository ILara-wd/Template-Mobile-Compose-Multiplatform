package com.invex.one.template.feature.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.one.template.core.designsystem.atom.AppFilterChip
import com.invex.one.template.core.designsystem.atom.StatusPill
import com.invex.one.template.core.designsystem.molecule.ScreenHeader
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.feature.games.component.GameCatalogCard
import com.invex.one.template.feature.games.component.style
import com.invex.one.template.feature.games.data.GameCatalog
import com.invex.one.template.feature.games.model.Game
import com.invex.one.template.feature.games.model.GameCategory
import com.invex.one.template.feature.games.model.GamesUiState

/** Stateful entry point: wires the [GamesViewModel] to the stateless [GamesScreen]. */
@Composable
fun GamesRoute(
    onPlayGame: (gameId: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: GamesViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GamesScreen(
        state = state,
        onCategorySelected = viewModel::onCategorySelected,
        onPlayGame = { onPlayGame(it.id, it.name) },
        modifier = modifier,
        contentPadding = contentPadding,
    )
}

@Composable
fun GamesScreen(
    state: GamesUiState,
    onCategorySelected: (GameCategory?) -> Unit,
    onPlayGame: (Game) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val spacing = AppTheme.spacing
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        fullWidthItem("header") {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                ScreenHeader(
                    title = stringResource(R.string.games_title),
                    action = {
                        StatusPill(pluralStringResource(R.plurals.games_available, state.games.size, state.games.size))
                    },
                    modifier = Modifier.padding(start = spacing.lg, end = spacing.lg, top = spacing.lg),
                )
                CategoryFilters(selected = state.selectedCategory, onSelected = onCategorySelected)
            }
        }
        itemsIndexed(state.visibleGames, key = { _, game -> game.id }) { index, game ->
            val isStartColumn = index % 2 == 0
            GameCatalogCard(
                game = game,
                onPlay = { onPlayGame(game) },
                // Outer cards keep the 16dp screen gutter; the grid gap separates the columns.
                modifier = Modifier.padding(
                    start = if (isStartColumn) spacing.lg else 0.dp,
                    end = if (isStartColumn) 0.dp else spacing.lg,
                ),
            )
        }
        if (state.visibleGames.isEmpty()) {
            fullWidthItem("empty") {
                Text(
                    stringResource(R.string.games_empty),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.xl),
                )
            }
        }
        // 16dp grid gap + 16dp = 32dp bottom padding from Figma.
        fullWidthItem("bottom") { Spacer(Modifier.height(spacing.lg)) }
    }
}

/** Horizontal row of category filter chips; `null` selects "Todos". */
@Composable
private fun CategoryFilters(
    selected: GameCategory?,
    onSelected: (GameCategory?) -> Unit,
) {
    val spacing = AppTheme.spacing
    LazyRow(
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        item(key = "all") {
            AppFilterChip(stringResource(R.string.games_filter_all), selected = selected == null, onClick = { onSelected(null) })
        }
        items(GameCategory.entries, key = { it.name }) { category ->
            AppFilterChip(
                text = stringResource(category.style().label),
                selected = selected == category,
                onClick = { onSelected(category) },
            )
        }
    }
}

private fun LazyGridScope.fullWidthItem(key: String, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Preview(widthDp = 390, heightDp = 1108)
@Composable
private fun GamesScreenPreview() {
    AppTheme {
        GamesScreen(
            state = GamesUiState(GameCatalog.games.take(7)),
            onCategorySelected = {},
            onPlayGame = {},
        )
    }
}
