package com.invex.one.template.feature.home

import androidx.lifecycle.ViewModel
import com.invex.one.template.feature.home.data.SampleHomeData
import com.invex.one.template.feature.home.model.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SampleHomeData.state)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
