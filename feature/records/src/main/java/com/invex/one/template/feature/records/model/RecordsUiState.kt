package com.invex.one.template.feature.records.model

import androidx.compose.runtime.Immutable

@Immutable
data class RecordsUiState(
    val summary: RecordsSummary,
    val records: List<GameRecord>,
)

@Immutable
data class RecordsSummary(
    val bestScore: Int,
    val totalGames: Int,
    val streakDays: Int,
)

@Immutable
data class GameRecord(
    val id: String,
    val rank: Int,
    val gameName: String,
    val category: RecordCategory,
    /** Mode / difficulty description, comes from game data (e.g. "Modo Clásico 4x4"). */
    val detail: String,
    val value: Int,
    val unit: RecordUnit,
)

enum class RecordCategory { Puzzle, Arcade, Logic, Memory, Classic, Sports }

/** What [GameRecord.value] measures. [Round] renders as "Ronda N" over "secuencia". */
enum class RecordUnit { Points, Moles, Completed, Round, Apples, Bounces }
