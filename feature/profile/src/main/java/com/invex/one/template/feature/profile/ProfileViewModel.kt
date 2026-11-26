package com.invex.one.template.feature.profile

import androidx.lifecycle.ViewModel
import com.invex.one.template.feature.profile.model.PlayerStats
import com.invex.one.template.feature.profile.model.ProfileCharacter
import com.invex.one.template.feature.profile.model.ProfileForm
import com.invex.one.template.feature.profile.model.ProfileUiState
import com.invex.one.template.feature.profile.model.ServerRegion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import java.util.TimeZone

/** Profile editing; there is no backend yet, so "save" only commits locally. */
class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SampleProfile.state)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onEditModeChange(enabled: Boolean) = _uiState.update { it.copy(editMode = enabled) }
    fun onCharacterSelected(character: ProfileCharacter) = updateForm { copy(character = character) }
    fun onGamertagChange(value: String) = updateForm { copy(gamertag = value.take(ProfileUiState.MAX_GAMERTAG)) }
    fun onFullNameChange(value: String) = updateForm { copy(fullName = value) }
    fun onEmailChange(value: String) = updateForm { state ->
        copy(email = value, emailVerified = value == state.savedForm.email && state.savedForm.emailVerified)
    }
    fun onBirthdateSelected(millis: Long?) = updateForm { copy(birthdateMillis = millis) }
    fun onRegionSelected(region: ServerRegion) = updateForm { copy(region = region) }
    fun onPublicProfileChange(value: Boolean) = updateForm { copy(publicProfile = value) }
    fun onStreakAlertsChange(value: Boolean) = updateForm { copy(streakAlerts = value) }

    fun onSave() = _uiState.update { it.copy(savedForm = it.form) }
    fun onDiscard() = _uiState.update { it.copy(form = it.savedForm) }

    private fun updateForm(transform: ProfileForm.(ProfileUiState) -> ProfileForm) =
        _uiState.update { it.copy(form = it.form.transform(it)) }
}

/** Placeholder profile matching the Figma frame until a real repository exists. */
internal object SampleProfile {
    private val birthdate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(2002, Calendar.AUGUST, 14)
    }.timeInMillis

    private val form = ProfileForm(
        character = ProfileCharacter.Astro,
        gamertag = "PixelMaster_99",
        fullName = "Valeria Gómez",
        email = "valeria.gomez@pixelarcade.io",
        emailVerified = true,
        birthdateMillis = birthdate,
        region = ServerRegion.Spain,
        publicProfile = true,
        streakAlerts = true,
    )

    val state = ProfileUiState(
        stats = PlayerStats(
            level = 8,
            rank = "Pro",
            league = "Liga Platino",
            memberSince = 2023,
            coins = 1420,
            streakDays = 5,
            season = 4,
            gamesPlayed = 142,
            bestScore = 4096,
            bestScoreGame = "2048",
            achievements = 12,
            achievementsTotal = 16,
            photo = R.drawable.sample_profile_photo,
        ),
        form = form,
        savedForm = form,
    )
}
