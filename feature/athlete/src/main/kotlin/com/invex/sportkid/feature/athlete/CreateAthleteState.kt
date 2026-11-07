package com.invex.sportkid.feature.athlete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.invex.sportkid.core.model.Belt
import com.invex.sportkid.core.model.Sport

/** Datos capturados en P04 al guardar. */
@Immutable
data class AthleteDraft(
    val name: String,
    val birthDateMillis: Long?,
    val colorIndex: Int,
    val sports: Set<Sport>,
    val belt: Belt?,
    val tkdSchool: String,
    val swimmingClub: String,
)

/** Estado del formulario de alta. Sobrevive a rotación y muerte de proceso. */
class CreateAthleteState(initial: AthleteDraft = Empty) {
    var name by mutableStateOf(initial.name)
    var birthDateMillis by mutableStateOf(initial.birthDateMillis)
    var colorIndex by mutableStateOf(initial.colorIndex)
    var sports by mutableStateOf(initial.sports)
    var belt by mutableStateOf(initial.belt)
    var tkdSchool by mutableStateOf(initial.tkdSchool)
    var swimmingClub by mutableStateOf(initial.swimmingClub)

    val canSave: Boolean get() = name.isNotBlank() && sports.isNotEmpty()

    fun toggleSport(sport: Sport, selected: Boolean) {
        sports = if (selected) sports + sport else sports - sport
    }

    fun toDraft() = AthleteDraft(name.trim(), birthDateMillis, colorIndex, sports, belt, tkdSchool.trim(), swimmingClub.trim())

    companion object {
        val Empty = AthleteDraft("", null, 0, emptySet(), null, "", "")

        val Saver = listSaver<CreateAthleteState, Any?>(
            save = {
                listOf(
                    it.name, it.birthDateMillis, it.colorIndex,
                    it.sports.map(Sport::name), it.belt?.name, it.tkdSchool, it.swimmingClub,
                )
            },
            restore = {
                @Suppress("UNCHECKED_CAST")
                CreateAthleteState(
                    AthleteDraft(
                        name = it[0] as String,
                        birthDateMillis = it[1] as Long?,
                        colorIndex = it[2] as Int,
                        sports = (it[3] as List<String>).map(Sport::valueOf).toSet(),
                        belt = (it[4] as String?)?.let(Belt::valueOf),
                        tkdSchool = it[5] as String,
                        swimmingClub = it[6] as String,
                    ),
                )
            },
        )
    }
}

@Composable
fun rememberCreateAthleteState(initial: AthleteDraft = CreateAthleteState.Empty): CreateAthleteState =
    rememberSaveable(saver = CreateAthleteState.Saver) { CreateAthleteState(initial) }
