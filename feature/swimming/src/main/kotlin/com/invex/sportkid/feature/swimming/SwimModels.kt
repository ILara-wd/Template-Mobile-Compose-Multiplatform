package com.invex.sportkid.feature.swimming

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.MedalResult
import com.invex.sportkid.core.model.PaymentStatus
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.model.SwimStyle
import com.invex.sportkid.core.model.SwimStyleStatus
import java.time.LocalDate
import java.util.Locale

/**
 * Deportista en el contexto de natación. `app` lo arma a partir del perfil del hijo
 * (los features no dependen entre sí). [colorIndex] apunta a `AthleteColors.all`.
 */
@Immutable
data class SwimAthlete(
    val id: String,
    val firstName: String,
    val fullName: String,
    val colorIndex: Int,
    val birthDate: LocalDate,
    val sports: List<Sport>,
    val club: String,
    val category: String,
    @DrawableRes val photoRes: Int? = null,
)

/** Estado de un estilo; [since] es la fecha de acreditación y [note] el detalle si aún no se domina. */
@Immutable
data class StyleProgress(val style: SwimStyle, val status: SwimStyleStatus, val since: LocalDate? = null, val note: String? = null)

/** Tiempo en centésimas de segundo. */
@JvmInline
value class SwimTime(val centis: Int) : Comparable<SwimTime> {
    override fun compareTo(other: SwimTime): Int = centis.compareTo(other.centis)

    /** "00:38.85". */
    fun format(): String = "%02d:%02d.%02d".format(Locale.ROOT, centis / 6000, centis / 100 % 60, centis % 100)

    /** "38.85" (segundos, para resultados). */
    fun seconds(): String = if (centis >= 6000) format() else "%d.%02d".format(Locale.ROOT, centis / 100, centis % 100)

    companion object {
        /** Desde los dígitos del campo enmascarado ("3885" = 00:38.85). */
        fun fromDigits(digits: String): SwimTime? {
            if (digits.length < 3) return null
            val padded = digits.padStart(6, '0')
            val minutes = padded.substring(0, 2).toInt()
            val seconds = padded.substring(2, 4).toInt()
            val centis = padded.substring(4, 6).toInt()
            if (seconds >= 60) return null
            return SwimTime(minutes * 6000 + seconds * 100 + centis)
        }
    }
}

/** Distancias de prueba en metros. */
val SwimDistances = listOf(25, 50, 100, 200)

/** Resultado de una prueba nadada: tiempo y lugar (1–3 = medalla). */
@Immutable
data class RaceResult(val style: SwimStyle, val distance: Int, val time: SwimTime?, val place: Int?, val relay: Boolean = false)

@Immutable
data class SwimMeet(
    val name: String,
    val level: String,
    val date: LocalDate,
    val venue: String,
    val costPesos: Int,
    val payment: PaymentStatus,
    val results: List<RaceResult>,
)

@Immutable
data class CoachNote(val coach: String, val goal: String, val text: String)

@StringRes
internal fun SwimStyle.labelRes(): Int = when (this) {
    SwimStyle.FREESTYLE -> R.string.swim_style_freestyle
    SwimStyle.BACKSTROKE -> R.string.swim_style_backstroke
    SwimStyle.BREASTSTROKE -> R.string.swim_style_breaststroke
    SwimStyle.BUTTERFLY -> R.string.swim_style_butterfly
    SwimStyle.MEDLEY -> R.string.swim_style_medley
}

@StringRes
internal fun SwimStyle.shortLabelRes(): Int = when (this) {
    SwimStyle.FREESTYLE -> R.string.swim_style_short_freestyle
    SwimStyle.BACKSTROKE -> R.string.swim_style_short_backstroke
    SwimStyle.BREASTSTROKE -> R.string.swim_style_short_breaststroke
    SwimStyle.BUTTERFLY -> R.string.swim_style_short_butterfly
    SwimStyle.MEDLEY -> R.string.swim_style_short_medley
}

@DrawableRes
internal fun SwimStyle.iconRes(): Int = when (this) {
    SwimStyle.FREESTYLE -> R.drawable.ic_swim_style_freestyle
    SwimStyle.BACKSTROKE -> R.drawable.ic_swim_style_backstroke
    SwimStyle.BREASTSTROKE -> R.drawable.ic_swim_style_breaststroke
    SwimStyle.BUTTERFLY -> R.drawable.ic_swim_style_butterfly
    SwimStyle.MEDLEY -> R.drawable.ic_swim_style_medley
}

@StringRes
internal fun SwimStyleStatus.labelRes(): Int = when (this) {
    SwimStyleStatus.MASTERED -> R.string.swim_status_mastered
    SwimStyleStatus.LEARNING -> R.string.swim_status_learning
    SwimStyleStatus.PENDING -> R.string.swim_status_pending
}

/** Medalla de un lugar: 1–3 = oro, plata, bronce. */
internal fun medalFor(place: Int?): MedalResult? = when (place) {
    1 -> MedalResult.GOLD
    2 -> MedalResult.SILVER
    3 -> MedalResult.BRONZE
    else -> null
}

@Composable
@ReadOnlyComposable
internal fun SwimAthlete.color(): Color =
    SportKidTheme.sportColors.athlete.all.let { it[colorIndex.mod(it.size)] }

@Composable
internal fun amountLabel(pesos: Int): String =
    stringResource(R.string.swim_amount, String.format(Locale("es", "MX"), "%,d", pesos))

/** Datos de demostración (Figma "Pestaña Natación"). TODO: repositorio de natación. */
object SwimSamples {
    val mateo = SwimAthlete(
        id = "mateo",
        firstName = "Mateo",
        fullName = "Mateo Gómez",
        colorIndex = 0,
        birthDate = LocalDate.of(2017, 5, 15),
        sports = listOf(Sport.TAEKWONDO, Sport.SWIMMING),
        club = "Club Acuático Olímpico",
        category = "Infantil A",
    )

    val styles = listOf(
        StyleProgress(SwimStyle.FREESTYLE, SwimStyleStatus.MASTERED, LocalDate.of(2024, 3, 12)),
        StyleProgress(SwimStyle.BACKSTROKE, SwimStyleStatus.MASTERED, LocalDate.of(2024, 5, 18)),
        StyleProgress(SwimStyle.BREASTSTROKE, SwimStyleStatus.MASTERED, LocalDate.of(2024, 8, 20)),
        StyleProgress(SwimStyle.BUTTERFLY, SwimStyleStatus.LEARNING, note = "En práctica de viraje y ondulación"),
        StyleProgress(SwimStyle.MEDLEY, SwimStyleStatus.PENDING),
    )

    /** Mejores tiempos por estilo y distancia (piscina corta). */
    val bestTimes: Map<SwimStyle, Map<Int, SwimTime>> = mapOf(
        SwimStyle.FREESTYLE to mapOf(25 to SwimTime(1842), 50 to SwimTime(3915), 100 to SwimTime(8840)),
        SwimStyle.BACKSTROKE to mapOf(25 to SwimTime(2110), 50 to SwimTime(4480)),
        SwimStyle.BREASTSTROKE to mapOf(25 to SwimTime(2350), 50 to SwimTime(4820)),
        SwimStyle.BUTTERFLY to mapOf(25 to SwimTime(2490)),
    )

    /** Récord del que más se presume (resaltado en la tabla). */
    val highlight: Pair<SwimStyle, Int> = SwimStyle.FREESTYLE to 50

    val meets = listOf(
        SwimMeet(
            "Torneo Estatal 100 m Libre y Dorso", "FMN Estatal", LocalDate.of(2024, 11, 24), "Alberca Olímpica CODE", 800, PaymentStatus.PAID,
            listOf(
                RaceResult(SwimStyle.FREESTYLE, 50, SwimTime(3915), 2),
                RaceResult(SwimStyle.BACKSTROKE, 50, SwimTime(4480), 4),
                RaceResult(SwimStyle.FREESTYLE, 100, null, 1, relay = true),
            ),
        ),
        SwimMeet(
            "Copa Acuática Infantil de Primavera", "Invitacional", LocalDate.of(2024, 5, 25), "Club Britania", 500, PaymentStatus.PAID,
            listOf(
                RaceResult(SwimStyle.FREESTYLE, 25, SwimTime(1842), 1),
                RaceResult(SwimStyle.BREASTSTROKE, 25, SwimTime(2350), 3),
            ),
        ),
    )

    val coachNote = CoachNote(
        coach = "Ramón",
        goal = "Salida desde el banco",
        text = "\"Mateo ha mejorado notablemente el codo alto en crol. Nos enfocaremos estas dos semanas en la entrada limpia para no perder segundos al despegue.\"",
    )
}
