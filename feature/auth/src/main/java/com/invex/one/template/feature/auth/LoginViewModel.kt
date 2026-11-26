package com.invex.one.template.feature.auth

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val rememberMe: Boolean = true,
    /** Minimum level shown next to the identifier label (Figma: "NIVEL 12 REQUERIDO"). */
    val requiredLevel: Int = 12,
)

/** Form state only; there is no auth backend yet, so submitting just completes the flow. */
class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentifierChange(value: String) = _uiState.update { it.copy(identifier = value) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value) }
    fun onTogglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun onRememberMeChange(value: Boolean) = _uiState.update { it.copy(rememberMe = value) }
}
