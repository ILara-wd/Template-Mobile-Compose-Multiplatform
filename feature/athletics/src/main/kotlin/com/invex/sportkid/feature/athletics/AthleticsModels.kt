package com.invex.sportkid.feature.athletics

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.MedalResult
import com.invex.sportkid.core.model.PaymentStatus
import com.invex.sportkid.core.model.Sport
import java.time.LocalDate
import java.util.Locale

/**
 * Deportista en el contexto de atletismo. `app` lo arma a partir del perfil del hijo
 * (los features no dependen entre sí). [colorIndex] apunta a `AthleteColors.all`.
 */
@Immutable
data class AthleticsAthlete(
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

/** Modalidad de la prueba (M14). */
enum class RaceType { Flat, Hurdles }

/** Tiempo de carrera en segundos. */
@JvmInline
value class RaceTime(val seconds: Int) : Comparable<RaceTime> {
    override fun compareTo(other: RaceTime): Int = seconds.compareTo(other.seconds)

    /** "14:52" (o "1:02:10" si pasa de una hora). */
    fun short(): String = if (seconds >= 3600) {
        "%d:%02d:%02d".format(Locale.ROOT, seconds / 3600, seconds / 60 % 60, seconds % 60)
    } else {
        "%02d:%02d".format(Locale.ROOT, seconds / 60, seconds % 60)
    }

    companion object {
        /** Desde los dígitos del campo `hh:mm:ss` ("1452" = 00:14:52). */
        fun fromDigits(digits: String): RaceTime? {
            if (digits.length < 3) return null
            val p = digits.padStart(6, '0')
            val (h, m, s) = Triple(p.substring(0, 2).toInt(), p.substring(2, 4).toInt(), p.substring(4, 6).toInt())
            if (m >= 60 || s >= 60) return null
            return RaceTime(h * 3600 + m * 60 + s)
        }
    }
}

/** Distancias rápidas en metros (P15). */
val RaceDistances = listOf(100, 400, 1000, 3000, 5000)

@Immutable
data class BestMark(val distance: Int, val type: RaceType, val time: RaceTime, val date: LocalDate, val medal: MedalResult?, val isRecord: Boolean = false)

@Immutable
data class MedalPhoto(val title: String, val year: Int, val medal: MedalResult, @DrawableRes val photoRes: Int)

@Immutable
data class Race(
    val name: String,
    val date: LocalDate,
    val venue: String,
    val distance: Int,
    val type: RaceType,
    val time: RaceTime,
    val result: MedalResult,
    val costPesos: Int,
    val payment: PaymentStatus,
    @DrawableRes val photoRes: Int? = null,
    val isRecord: Boolean = false,
)

/** "500 m", "3 K". */
@Composable
internal fun distanceLabel(meters: Int): String =
    if (meters >= 1000 && meters % 1000 == 0) stringResource(R.string.ath_distance_km, meters / 1000) else stringResource(R.string.ath_distance_m, meters)

@Composable
internal fun RaceType.label(long: Boolean = false): String = stringResource(
    when (this) {
        RaceType.Flat -> if (long) R.string.ath_type_flat_long else R.string.ath_type_flat
        RaceType.Hurdles -> if (long) R.string.ath_type_hurdles_long else R.string.ath_type_hurdles
    },
)

@Composable
@ReadOnlyComposable
internal fun AthleticsAthlete.color(): Color =
    SportKidTheme.sportColors.athlete.all.let { it[colorIndex.mod(it.size)] }

@Composable
internal fun amountLabel(pesos: Int): String =
    stringResource(R.string.ath_amount, String.format(Locale("es", "MX"), "%,d", pesos))

/** Datos de demostración (Figma "Pestaña Atletismo"). TODO: repositorio de atletismo. */
object AthleticsSamples {
    val sofia = AthleticsAthlete(
        id = "sofia",
        firstName = "Sofía",
        fullName = "Sofía Gómez",
        colorIndex = 1,
        birthDate = LocalDate.of(2019, 3, 2),
        sports = listOf(Sport.SWIMMING, Sport.ATHLETICS),
        club = "Club Halcones Atletismo",
        category = "Infantil A",
    )

    val bestMarks = listOf(
        BestMark(1000, RaceType.Flat, RaceTime(258), LocalDate.of(2024, 10, 12), MedalResult.GOLD),
        BestMark(3000, RaceType.Flat, RaceTime(892), LocalDate.of(2024, 11, 24), MedalResult.GOLD, isRecord = true),
        BestMark(500, RaceType.Hurdles, RaceTime(155), LocalDate.of(2024, 8, 17), MedalResult.SILVER),
    )

    val gallery = listOf(
        MedalPhoto("3K Infantil", 2024, MedalResult.GOLD, R.drawable.img_sample_ath_g_3k),
        MedalPhoto("1K Relevos", 2024, MedalResult.GOLD, R.drawable.img_sample_ath_g_relay),
        MedalPhoto("500 m Pista", 2024, MedalResult.SILVER, R.drawable.img_sample_ath_g_500),
        MedalPhoto("Cross Country", 2023, MedalResult.BRONZE, R.drawable.img_sample_ath_g_cross),
    )

    const val NEXT_GOAL = "5K Serial Primavera"

    val races = listOf(
        Race(
            "Carrera Infantil 3K Primavera", LocalDate.of(2024, 11, 24), "Pista Olímpica CODE", 3000, RaceType.Flat,
            RaceTime(892), MedalResult.GOLD, 350, PaymentStatus.PAID, R.drawable.img_sample_ath_r_3k, isRecord = true,
        ),
        Race(
            "Serial Atlético Escolar 1K", LocalDate.of(2024, 10, 12), "Deportivo Benito Juárez", 1000, RaceType.Flat,
            RaceTime(258), MedalResult.GOLD, 200, PaymentStatus.PAID, R.drawable.img_sample_ath_r_1k, isRecord = true,
        ),
        Race(
            "Circuito Infantil con Obstáculos", LocalDate.of(2024, 8, 17), "Parque Metropolitano", 500, RaceType.Hurdles,
            RaceTime(155), MedalResult.SILVER, 250, PaymentStatus.PAID, R.drawable.img_sample_ath_r_hurdles,
        ),
    )

    /** Mejor tiempo previo por distancia y modalidad (para detectar récords en P15). */
    fun previousBest(distance: Int, type: RaceType): RaceTime? =
        bestMarks.firstOrNull { it.distance == distance && it.type == type }?.time
}
