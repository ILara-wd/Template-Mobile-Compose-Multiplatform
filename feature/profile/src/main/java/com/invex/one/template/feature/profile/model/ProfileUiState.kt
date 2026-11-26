package com.invex.one.template.feature.profile.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.feature.profile.R
import java.util.Calendar
import java.util.TimeZone

/** Read-only player summary shown in the hero and metrics. */
@Immutable
data class PlayerStats(
    val level: Int,
    val rank: String,
    val league: String,
    val memberSince: Int,
    val coins: Int,
    val streakDays: Int,
    val season: Int,
    val gamesPlayed: Int,
    val bestScore: Int,
    val bestScoreGame: String,
    val achievements: Int,
    val achievementsTotal: Int,
    @param:DrawableRes val photo: Int,
)

/** Editable fields; a saved copy is kept to support "Descartar Cambios". */
@Immutable
data class ProfileForm(
    val character: ProfileCharacter,
    val gamertag: String,
    val fullName: String,
    val email: String,
    val emailVerified: Boolean,
    /** UTC-midnight millis. */
    val birthdateMillis: Long?,
    val region: ServerRegion,
    val publicProfile: Boolean,
    val streakAlerts: Boolean,
)

@Immutable
data class ProfileUiState(
    val stats: PlayerStats,
    val form: ProfileForm,
    val savedForm: ProfileForm,
    val editMode: Boolean = true,
) {
    val hasChanges: Boolean get() = form != savedForm
    val gamertagAvailable: Boolean get() = form.gamertag.trim().length >= MIN_GAMERTAG

    companion object {
        const val MIN_GAMERTAG = 3
        const val MAX_GAMERTAG = 20
    }
}

/** Characters unlocked by the player. */
enum class ProfileCharacter(val displayName: String, @param:DrawableRes val icon: Int) {
    Astro("Astro", AppIcons.CharAstro),
    Valeria("Valeria", AppIcons.CharValeria),
    CiberBot("CiberBot", AppIcons.CharCiberBot),
    Draco("Draco", AppIcons.CharDraco),
}

enum class ServerRegion(val flag: String, @param:StringRes val country: Int, @param:StringRes val server: Int) {
    Spain("🇪🇸", R.string.profile_region_spain, R.string.profile_server_eu_south),
    Mexico("🇲🇽", R.string.profile_region_mexico, R.string.profile_server_north_america),
    UnitedStates("🇺🇸", R.string.profile_region_usa, R.string.profile_server_north_america),
    Colombia("🇨🇴", R.string.profile_region_colombia, R.string.profile_server_south_america),
    Argentina("🇦🇷", R.string.profile_region_argentina, R.string.profile_server_south_america),
}

/** Age bracket used for tournament matchmaking. */
enum class AgeBracket { Kids, Junior, Senior }

/** Full years between a UTC-midnight birthdate and today. */
fun ageInYears(birthdateMillis: Long, nowMillis: Long = System.currentTimeMillis()): Int {
    val utc = TimeZone.getTimeZone("UTC")
    val birth = Calendar.getInstance(utc).apply { timeInMillis = birthdateMillis }
    val now = Calendar.getInstance(utc).apply { timeInMillis = nowMillis }
    var age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
    if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--
    return age.coerceAtLeast(0)
}

fun ageBracket(age: Int): AgeBracket = when {
    age >= 18 -> AgeBracket.Senior
    age >= 13 -> AgeBracket.Junior
    else -> AgeBracket.Kids
}
