package com.invex.sportkid.feature.agenda

import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.PaymentStatus
import com.invex.sportkid.core.model.Sport
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.util.Locale

/** Hijo tal como lo muestra la agenda. [colorIndex] apunta a `AthleteColors.all`. */
@Immutable
data class AgendaAthlete(
    val id: String,
    val name: String,
    val colorIndex: Int,
    val ageYears: Int,
    val sports: List<Sport>,
    @DrawableRes val avatarRes: Int? = null,
)

/**
 * Evento del calendario. [special] marca torneos, carreras y exámenes que se pintan con el punto
 * de "Torneo / Especial". [tip] es el recordatorio que se muestra al elegir ese día.
 */
@Immutable
data class AgendaEvent(
    val id: String,
    val date: LocalDate,
    val sport: Sport,
    val athleteId: String,
    val title: String,
    val start: LocalTime,
    val end: LocalTime,
    val subtitle: String? = null,
    val place: String? = null,
    val costLabel: String? = null,
    val costPesos: Int? = null,
    val payment: PaymentStatus? = null,
    val special: Boolean = false,
    val tip: String? = null,
)

/** Color del hijo según su índice en la paleta fija. */
@Composable
@ReadOnlyComposable
internal fun AgendaAthlete.color(): Color =
    SportKidTheme.sportColors.athlete.all.let { it[colorIndex.mod(it.size)] }

// --- Formato de fechas es-MX (independiente del idioma del dispositivo) ---

@Composable
private fun names(@ArrayRes res: Int): Array<String> = stringArrayResource(res)

/** "Noviembre 2025". */
@Composable
internal fun YearMonth.label(): String =
    stringResource(R.string.agenda_month_year, names(R.array.agenda_months)[monthValue - 1], year)

/** "Viernes 14 de noviembre". */
@Composable
internal fun LocalDate.longLabel(): String = stringResource(
    R.string.agenda_day_long,
    names(R.array.agenda_weekdays)[dayOfWeek.value % 7],
    dayOfMonth,
    names(R.array.agenda_months)[monthValue - 1].lowercase(),
)

/** "Sáb 15 nov, 2025". */
@Composable
internal fun LocalDate.shortLabel(): String = stringResource(
    R.string.agenda_date_short,
    names(R.array.agenda_weekdays_short)[dayOfWeek.value % 7],
    dayOfMonth,
    names(R.array.agenda_months_short)[monthValue - 1],
    year,
)

/** "10:00 hrs". */
@Composable
internal fun LocalTime.label(): String = stringResource(R.string.agenda_time, hour, minute)

/** "09:00 - 11:30 hrs". */
@Composable
internal fun AgendaEvent.timeLabel(): String =
    stringResource(R.string.agenda_time_range, start.hour, start.minute, end.hour, end.minute)

/** "$650 MXN". */
@Composable
internal fun amountLabel(pesos: Int): String =
    stringResource(R.string.agenda_amount, String.format(EsMx, "%,d", pesos))

private val EsMx = Locale("es", "MX")

@Composable
internal fun commitmentsLabel(count: Int): String = pluralStringResource(R.plurals.agenda_commitments, count, count)

/**
 * Datos de demostración (Figma "SportKid - Agenda deportiva"), anclados al mes que se pasa para
 * que la agenda siempre tenga contenido. TODO: reemplazar por el repositorio de eventos.
 */
object AgendaSamples {
    const val MATEO = "mateo"
    const val SOFIA = "sofia"

    val athletes = listOf(
        AgendaAthlete(MATEO, "Mateo", colorIndex = 0, ageYears = 9, sports = listOf(Sport.TAEKWONDO, Sport.SWIMMING), avatarRes = R.drawable.img_sample_mateo),
        AgendaAthlete(SOFIA, "Sofía", colorIndex = 1, ageYears = 7, sports = listOf(Sport.SWIMMING), avatarRes = R.drawable.img_sample_sofia),
    )

    /** Día con el examen y la clase de natación del diseño. */
    const val FEATURED_DAY = 14

    fun events(month: YearMonth): List<AgendaEvent> {
        fun day(d: Int) = month.atDay(d.coerceAtMost(month.lengthOfMonth()))
        fun tkd(d: Int) = AgendaEvent(
            id = "tkd-$d", date = day(d), sport = Sport.TAEKWONDO, athleteId = MATEO,
            title = "Clase de Poomsae y Combate", start = LocalTime.of(17, 30), end = LocalTime.of(19, 0),
            subtitle = "Taekwondo Infantil · Profesor Kim", place = "Doyang Central Coyoacán",
        )
        fun swim(d: Int) = AgendaEvent(
            id = "swim-$d", date = day(d), sport = Sport.SWIMMING, athleteId = SOFIA,
            title = "Entrenamiento de técnica", start = LocalTime.of(16, 0), end = LocalTime.of(17, 0),
            subtitle = "Categoría Infantil B", place = "Club Acuático Olímpico Los Pinos",
        )
        return listOf(
            tkd(1), swim(4), tkd(6), swim(8), tkd(8), swim(11), tkd(13),
            AgendaEvent(
                id = "exam", date = day(FEATURED_DAY), sport = Sport.TAEKWONDO, athleteId = MATEO,
                title = "Examen de cinta verde", start = LocalTime.of(9, 0), end = LocalTime.of(11, 30),
                subtitle = "Taekwondo Infantil · Profesor Kim", place = "Doyang Central Polideportivo Norte",
                costLabel = "Cuota de graduación", costPesos = 650, payment = PaymentStatus.PENDING,
                tip = "Recuerda llevar cinta blanca y uniforme limpio (Dobok) antes de las 8:45 hrs.",
            ),
            AgendaEvent(
                id = "swim-free", date = day(FEATURED_DAY), sport = Sport.SWIMMING, athleteId = SOFIA,
                title = "Entrenamiento libre pre-competencia", start = LocalTime.of(16, 0), end = LocalTime.of(17, 30),
                subtitle = "Categoría Infantil B · Carril 4", place = "Club Acuático Olímpico Los Pinos",
                costLabel = "Uso de alberca olímpica", costPesos = 200, payment = PaymentStatus.PAID,
            ),
            AgendaEvent(
                id = "swim-meet", date = day(18), sport = Sport.SWIMMING, athleteId = SOFIA,
                title = "Torneo Relámpago 50m Libre", start = LocalTime.of(8, 0), end = LocalTime.of(12, 0),
                place = "Alberca Olímpica", costLabel = "Inscripción", costPesos = 350,
                payment = PaymentStatus.PENDING, special = true,
            ),
            tkd(20),
            AgendaEvent(
                id = "race", date = day(22), sport = Sport.ATHLETICS, athleteId = MATEO,
                title = "Carrera 5K infantil", start = LocalTime.of(7, 30), end = LocalTime.of(9, 0),
                place = "Pista Revolución", costLabel = "Inscripción", costPesos = 280,
                payment = PaymentStatus.PAID, special = true,
            ),
            swim(25), tkd(27), tkd(29),
        )
    }
}
