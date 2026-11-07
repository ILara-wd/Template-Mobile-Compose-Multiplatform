package com.invex.sportkid.feature.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.invex.sportkid.core.model.Sport
import java.time.LocalDate
import java.time.YearMonth

/** Vista de la agenda (control segmentado "Mes / Lista"). */
enum class AgendaView { Month, List }

/**
 * Estado de P06: mes visible, día elegido, vista y filtros. Sobrevive a rotación y muerte de
 * proceso.
 *
 * Filtros: [athleteId] `null` = "Todos"; [sports] vacío = todos los deportes.
 */
@Stable
class AgendaState(
    month: YearMonth,
    selectedDate: LocalDate,
    view: AgendaView = AgendaView.Month,
    athleteId: String? = null,
    sports: Set<Sport> = emptySet(),
) {
    var month by mutableStateOf(month)
    var selectedDate by mutableStateOf(selectedDate)
    var view by mutableStateOf(view)
    var athleteId by mutableStateOf(athleteId)
    var sports by mutableStateOf(sports)

    fun select(date: LocalDate) {
        selectedDate = date
        month = YearMonth.from(date)
    }

    fun showMonth(target: YearMonth) {
        month = target
        if (YearMonth.from(selectedDate) != target) selectedDate = target.atDay(1)
    }

    fun toggleSport(sport: Sport) {
        sports = if (sport in sports) sports - sport else sports + sport
    }

    fun matches(event: AgendaEvent): Boolean =
        (athleteId == null || event.athleteId == athleteId) && (sports.isEmpty() || event.sport in sports)

    companion object {
        val Saver = listSaver<AgendaState, Any?>(
            save = {
                listOf(it.month.toString(), it.selectedDate.toEpochDay(), it.view.name, it.athleteId, it.sports.map(Sport::name))
            },
            restore = {
                @Suppress("UNCHECKED_CAST")
                AgendaState(
                    month = YearMonth.parse(it[0] as String),
                    selectedDate = LocalDate.ofEpochDay(it[1] as Long),
                    view = AgendaView.valueOf(it[2] as String),
                    athleteId = it[3] as String?,
                    sports = (it[4] as List<String>).map(Sport::valueOf).toSet(),
                )
            },
        )
    }
}

@Composable
fun rememberAgendaState(initialDate: LocalDate = LocalDate.now()): AgendaState =
    rememberSaveable(saver = AgendaState.Saver) { AgendaState(YearMonth.from(initialDate), initialDate) }
