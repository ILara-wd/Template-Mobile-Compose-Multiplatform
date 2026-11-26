package com.invex.one.template.feature.home.data

import com.invex.one.template.feature.home.model.DailyChallenge
import com.invex.one.template.feature.home.model.GameCategory
import com.invex.one.template.feature.home.model.HomeUiState
import com.invex.one.template.feature.home.model.QuickStats
import com.invex.one.template.feature.home.model.RecentGame

/** Placeholder data matching the Figma frame until a real repository exists. */
internal object SampleHomeData {
    val state = HomeUiState(
        level = 8,
        quickStats = QuickStats(streakDays = 5, totalGames = 142, weeklyTopPercent = 5),
        dailyChallenge = DailyChallenge(gameName = "2048", targetScore = 2048, currentScore = 1330, xpReward = 150),
        mostPlayed = listOf(
            RecentGame("2048", "2048", GameCategory.NumberPuzzle, timesPlayed = 24, bestScore = 4096),
            RecentGame("whack", "Whack-a-Mole", GameCategory.Reflexes, timesPlayed = 19, bestScore = 68, bestScoreMultiplier = 4),
            RecentGame("snake", "Snake Clásico", GameCategory.Arcade, timesPlayed = 15, bestScore = 240),
        ),
    )
}
