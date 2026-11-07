package com.invex.sportkid.feature.tkd

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Belt
import com.invex.sportkid.core.model.MedalResult
import com.invex.sportkid.core.model.Sport
import java.time.LocalDate
import java.util.Locale

/**
 * Deportista en el contexto de Taekwondo. `app` lo arma a partir del perfil del hijo
 * (los features no dependen entre sí). [colorIndex] apunta a `AthleteColors.all`.
 */
@Immutable
data class TkdAthlete(
    val id: String,
    val firstName: String,
    val fullName: String,
    val colorIndex: Int,
    val birthDate: LocalDate,
    val sports: List<Sport>,
    val belt: Belt,
    val beltSince: LocalDate?,
    val school: String,
    val instructor: String,
    val federation: String = "Federación Mexicana de Taekwondo (FMTKD)",
    val schoolAddress: String? = null,
    @DrawableRes val photoRes: Int? = null,
)

@Immutable
data class BeltPromotion(
    val belt: Belt,
    val date: LocalDate,
    val place: String,
    val costPesos: Int? = null,
    val paid: Boolean = true,
    /** Primer registro en la escuela (sin examen). */
    val enrollment: Boolean = false,
)

enum class TournamentModality { Combat, Forms }

@Immutable
data class Tournament(
    val name: String,
    val date: LocalDate,
    val place: String,
    val modality: TournamentModality,
    val division: String,
    val result: MedalResult,
)

/** Resultado de un examen de cinta (README §5.3). */
enum class ExamResult { Pending, Passed, Failed }

@Composable
@ReadOnlyComposable
internal fun TkdAthlete.color(): Color =
    SportKidTheme.sportColors.athlete.all.let { it[colorIndex.mod(it.size)] }

/** "$650 MXN". */
@Composable
internal fun amountLabel(pesos: Int): String =
    stringResource(R.string.tkd_amount, String.format(Locale("es", "MX"), "%,d", pesos))

/**
 * Datos de demostración (Figma "Pestaña Taekwondo"). TODO: reemplazar por el repositorio de
 * exámenes y torneos.
 */
object TkdSamples {
    val mateo = TkdAthlete(
        id = "mateo",
        firstName = "Mateo",
        fullName = "Mateo Gómez",
        colorIndex = 0,
        birthDate = LocalDate.of(2017, 5, 15),
        sports = listOf(Sport.TAEKWONDO, Sport.SWIMMING),
        belt = Belt.GREEN,
        beltSince = LocalDate.of(2024, 5, 18),
        school = "Doyang Central Polideportivo",
        instructor = "Profesor Min-Soo Kim",
        schoolAddress = "Av. Real de Minas 402, Benito Juárez",
    )

    val promotions = listOf(
        BeltPromotion(Belt.GREEN, LocalDate.of(2024, 5, 18), "Doyang Central Polideportivo", costPesos = 650),
        BeltPromotion(Belt.ORANGE, LocalDate.of(2023, 12, 10), "Doyang San Jerónimo", costPesos = 600),
        BeltPromotion(Belt.YELLOW, LocalDate.of(2023, 7, 15), "Gimnasio Benito Juárez", costPesos = 550),
        BeltPromotion(Belt.WHITE, LocalDate.of(2023, 1, 12), "Ingreso oficial · Doyang Central", enrollment = true),
    )

    val tournaments = listOf(
        Tournament(
            "Copa Abierta Metropolitana 2024", LocalDate.of(2024, 9, 28), "Gimnasio Olímpico Juan de la Barrera",
            TournamentModality.Combat, "División infantil -32 kg", MedalResult.GOLD,
        ),
        Tournament(
            "Torneo Estatal Infantil de TKD", LocalDate.of(2024, 6, 16), "Domo Polideportivo CODE",
            TournamentModality.Forms, "Individual cinta verde", MedalResult.SILVER,
        ),
        Tournament(
            "Intercolegial Marcial Copa Tigres", LocalDate.of(2024, 3, 2), "Colegio Franco Mexicano",
            TournamentModality.Combat, "Cuartos de final", MedalResult.PARTICIPATION,
        ),
    )
}
