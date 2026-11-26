package com.invex.one.template.feature.records.data

import com.invex.one.template.feature.records.model.GameRecord
import com.invex.one.template.feature.records.model.RecordCategory
import com.invex.one.template.feature.records.model.RecordUnit
import com.invex.one.template.feature.records.model.RecordsSummary
import com.invex.one.template.feature.records.model.RecordsUiState

/** Placeholder data matching the Figma frame until a real repository exists. */
internal object SampleRecordsData {
    val state = RecordsUiState(
        summary = RecordsSummary(bestScore = 4096, totalGames = 142, streakDays = 5),
        records = listOf(
            GameRecord("2048", 1, "2048", RecordCategory.Puzzle, "Modo Clásico 4x4", 4096, RecordUnit.Points),
            GameRecord("whack", 2, "Whack-a-Mole", RecordCategory.Arcade, "Combo x4 • Rápido", 68, RecordUnit.Moles),
            GameRecord("maze", 3, "Laberinto", RecordCategory.Logic, "Cuadrícula 15x15", 12, RecordUnit.Completed),
            GameRecord("simon", 4, "Simón Dice", RecordCategory.Memory, "Audio & Luces", 9, RecordUnit.Round),
            GameRecord("snake", 5, "Snake", RecordCategory.Classic, "Velocidad x2", 240, RecordUnit.Apples),
            GameRecord("pong", 6, "Pong", RecordCategory.Sports, "vs CPU Difícil", 34, RecordUnit.Bounces),
            GameRecord("invaders", 7, "Space Invaders", RecordCategory.Arcade, "Oleada 6", 1450, RecordUnit.Points),
        ),
    )
}
