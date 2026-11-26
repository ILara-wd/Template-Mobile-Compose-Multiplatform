package com.invex.one.template.feature.records

import androidx.lifecycle.ViewModel
import com.invex.one.template.feature.records.data.SampleRecordsData
import com.invex.one.template.feature.records.model.RecordsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecordsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SampleRecordsData.state)
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()
}
