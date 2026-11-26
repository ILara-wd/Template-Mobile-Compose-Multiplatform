package com.invex.one.template.feature.games.model

import androidx.compose.runtime.Immutable

@Immutable
data class GamesUiState(
    val games: List<Game>,
    /** `null` means "Todos". */
    val selectedCategory: GameCategory? = null,
) {
    val visibleGames: List<Game>
        get() = selectedCategory?.let { category -> games.filter { it.category == category } } ?: games
}

/**
 * A playable mini game. [id] is the stable key the app uses to open the game screen
 * (shared with home and records).
 */
@Immutable
data class Game(
    val id: String,
    val name: String,
    val description: String,
    val category: GameCategory,
)

/** Declaration order is the order of the filter chips. */
enum class GameCategory { Puzzle, Reflexes, Arcade, Logic, Memory, Retro, Action }
