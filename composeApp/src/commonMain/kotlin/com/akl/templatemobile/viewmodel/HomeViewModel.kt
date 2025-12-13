package com.akl.templatemobile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akl.templatemobile.core.network.DragonBallService
import com.akl.templatemobile.core.network.RemoteResult
import com.akl.templatemobile.model.CharacterDBZ
import kotlinx.coroutines.launch

class HomeViewModel(
    val service: DragonBallService
) : ViewModel() {

    var uiState by mutableStateOf(value = UiState())
        private set

    var uiStateDetail by mutableStateOf(value = UIStateDetail())
        private set

    fun getGreeting() {
        viewModelScope.launch {
            val char = service.getAllCharacters()
            uiState = UiState(greeting = char)
        }
    }

    fun getCharacterById(id: Int) {
        viewModelScope.launch {
            val character = service.getCharacterById(id)
            uiStateDetail = UIStateDetail(character = character)
        }
    }

}

data class UIStateDetail(
    val character: CharacterDBZ? = null
)

data class UiState(
    val greeting: RemoteResult? = null
)