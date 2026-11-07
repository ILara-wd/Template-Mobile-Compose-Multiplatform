package com.invex.sportkid.feature.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.invex.sportkid.core.model.Sport
import java.time.LocalDate
import java.time.LocalTime

/** Recordatorio familiar (M12). */
enum class TrainingReminder { None, OneHour, OneDay, TwoDays }

/** Duraciones rápidas del bloque, en minutos. */
val TrainingDurations = listOf(45, 60, 90, 120)

/** Datos capturados en P07 al guardar. */
@Immutable
data class TrainingDraft(
    val athleteId: String?,
    val sport: Sport?,
    val date: LocalDate,
    val start: LocalTime,
    val durationMinutes: Int,
    val place: String,
    val costPesos: Int?,
    val paid: Boolean,
    val reminder: TrainingReminder,
    val notes: String,
)

/** Estado del formulario "Nuevo entrenamiento". Sobrevive a rotación y muerte de proceso. */
@Stable
class NewTrainingState(initial: TrainingDraft) {
    var athleteId by mutableStateOf(initial.athleteId)
    var sport by mutableStateOf(initial.sport)
    var date by mutableStateOf(initial.date)
    var start by mutableStateOf(initial.start)
    var durationMinutes by mutableStateOf(initial.durationMinutes)
    var place by mutableStateOf(initial.place)
    var cost by mutableStateOf(initial.costPesos?.toString().orEmpty())
    var paid by mutableStateOf(initial.paid)
    var reminder by mutableStateOf(initial.reminder)
    var notes by mutableStateOf(initial.notes)

    val canSave: Boolean get() = athleteId != null && sport != null && place.isNotBlank()

    fun toDraft() = TrainingDraft(
        athleteId, sport, date, start, durationMinutes, place.trim(), cost.toIntOrNull(), paid, reminder, notes.trim(),
    )

    companion object {
        /** Valores iniciales: mañana a las 10:00, 1 h, recordatorio un día antes. */
        fun empty(today: LocalDate = LocalDate.now(), athleteId: String? = null, sport: Sport? = null) = TrainingDraft(
            athleteId = athleteId,
            sport = sport,
            date = today.plusDays(1),
            start = LocalTime.of(10, 0),
            durationMinutes = 60,
            place = "",
            costPesos = null,
            paid = false,
            reminder = TrainingReminder.OneDay,
            notes = "",
        )

        val Saver = listSaver<NewTrainingState, Any?>(
            save = {
                listOf(
                    it.athleteId, it.sport?.name, it.date.toEpochDay(), it.start.toSecondOfDay(), it.durationMinutes,
                    it.place, it.cost, it.paid, it.reminder.name, it.notes,
                )
            },
            restore = {
                NewTrainingState(
                    TrainingDraft(
                        athleteId = it[0] as String?,
                        sport = (it[1] as String?)?.let(Sport::valueOf),
                        date = LocalDate.ofEpochDay(it[2] as Long),
                        start = LocalTime.ofSecondOfDay((it[3] as Int).toLong()),
                        durationMinutes = it[4] as Int,
                        place = it[5] as String,
                        costPesos = (it[6] as String).toIntOrNull(),
                        paid = it[7] as Boolean,
                        reminder = TrainingReminder.valueOf(it[8] as String),
                        notes = it[9] as String,
                    ),
                )
            },
        )
    }
}

@Composable
fun rememberNewTrainingState(initial: TrainingDraft = NewTrainingState.empty()): NewTrainingState =
    rememberSaveable(saver = NewTrainingState.Saver) { NewTrainingState(initial) }
