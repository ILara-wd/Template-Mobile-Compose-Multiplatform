package com.invex.one.template.feature.auth

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.min
import kotlin.math.roundToInt

/** Avatars offered at sign up. Names are part of the avatar catalog, not UI copy. */
enum class GamerAvatar(val displayName: String, @param:DrawableRes val image: Int) {
    CiberBot("CiberBot", R.drawable.avatar_ciberbot),
    Valeria("Valeria", R.drawable.avatar_valeria),
    Draco("Draco", R.drawable.avatar_draco),
    Astro("Astro", R.drawable.avatar_astro),
}

/** 0 = empty, 1 weak … 4 strong; [percent] is shown next to the label. */
@Immutable
data class PasswordStrength(val level: Int, val percent: Int) {
    companion object {
        val Empty = PasswordStrength(0, 0)

        /** Character variety (60%) + length up to 12 chars (40%). */
        fun of(password: String): PasswordStrength {
            if (password.isEmpty()) return Empty
            val checks: List<(Char) -> Boolean> = listOf(
                { it.isLowerCase() },
                { it.isUpperCase() },
                { it.isDigit() },
                { !it.isLetterOrDigit() },
            )
            val variety = checks.count { check -> password.any(check) }
            val percent = ((variety / 4f) * 60 + (min(password.length, 12) / 12f) * 40).roundToInt()
            val level = when {
                percent < 40 -> 1
                percent < 60 -> 2
                percent < 80 -> 3
                else -> 4
            }
            return PasswordStrength(level, percent)
        }
    }
}

@Immutable
data class RegisterUiState(
    val avatar: GamerAvatar = GamerAvatar.Valeria,
    val gamertag: String = "",
    val email: String = "",
    /** UTC midnight millis, as returned by the Material date picker. */
    val birthdateMillis: Long? = null,
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmVisible: Boolean = false,
    val acceptedTerms: Boolean = false,
    val welcomeBonus: Int = 250,
) {
    /** Placeholder rule until there is a backend lookup. */
    val gamertagAvailable: Boolean get() = gamertag.trim().length >= MIN_GAMERTAG
    val strength: PasswordStrength get() = PasswordStrength.of(password)
    val passwordsMatch: Boolean? get() = if (confirmPassword.isEmpty()) null else confirmPassword == password

    companion object {
        const val MIN_GAMERTAG = 3
    }
}

/** Form state only; there is no account backend yet, so submitting just completes the flow. */
class RegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onAvatarSelected(avatar: GamerAvatar) = _uiState.update { it.copy(avatar = avatar) }
    fun onGamertagChange(value: String) = _uiState.update { it.copy(gamertag = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onBirthdateSelected(millis: Long?) = _uiState.update { it.copy(birthdateMillis = millis) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value) }
    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value) }
    fun onTogglePassword() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun onToggleConfirm() = _uiState.update { it.copy(confirmVisible = !it.confirmVisible) }
    fun onTermsChange(value: Boolean) = _uiState.update { it.copy(acceptedTerms = value) }
}
