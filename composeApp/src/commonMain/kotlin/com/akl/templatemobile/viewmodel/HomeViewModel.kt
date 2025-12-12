package com.akl.templatemobile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akl.templatemobile.core.network.DragonBallService
import com.akl.templatemobile.core.network.RemoteResult
import kotlinx.coroutines.launch

class HomeViewModel(
    val service: DragonBallService
) : ViewModel() {

    var uiState by mutableStateOf(value = UiState())
        private set

    fun getGreeting() {
        viewModelScope.launch {
            val char = service.getAllCharacters()
            uiState = UiState(greeting = char)
        }
    }

}


data class UiState(
    val greeting: RemoteResult? = null
)