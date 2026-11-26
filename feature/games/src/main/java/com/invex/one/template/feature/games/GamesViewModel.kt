package com.invex.one.template.feature.games

import androidx.lifecycle.ViewModel
import com.invex.one.template.feature.games.data.GameCatalog
import com.invex.one.template.feature.games.model.GameCategory
import com.invex.one.template.feature.games.model.GamesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GamesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GamesUiState(games = GameCatalog.games))
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()

    fun onCategorySelected(category: GameCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}
