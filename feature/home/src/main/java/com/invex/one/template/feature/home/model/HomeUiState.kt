package com.invex.one.template.feature.home.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

@Immutable
data class HomeUiState(
    val player: PlayerSummary,
    val quickStats: QuickStats,
    val dailyChallenge: DailyChallenge,
    val mostPlayed: List<RecentGame>,
)

@Immutable
data class PlayerSummary(
    val level: Int,
    val streakDays: Int,
    val coins: Int,
    @param:DrawableRes val avatar: Int,
)

@Immutable
data class QuickStats(
    val streakDays: Int,
    val totalGames: Int,
    val weeklyTopPercent: Int,
)

@Immutable
data class DailyChallenge(
    val gameName: String,
    val targetScore: Int,
    val currentScore: Int,
    val xpReward: Int,
) {
    val progress: Float get() = if (targetScore == 0) 0f else currentScore.toFloat() / targetScore
    val percent: Int get() = (progress * 100).roundToInt().coerceIn(0, 100)
}

@Immutable
data class RecentGame(
    val id: String,
    val name: String,
    val category: GameCategory,
    val timesPlayed: Int,
    val bestScore: Int,
    val bestScoreMultiplier: Int? = null,
)

enum class GameCategory { NumberPuzzle, Reflexes, Arcade }
