package com.invex.sportkid.feature.athlete

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Belt
import com.invex.sportkid.core.model.PaymentStatus
import com.invex.sportkid.core.model.Sport
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Immutable
data class Medals(val gold: Int = 0, val silver: Int = 0, val bronze: Int = 0) {
    val total: Int get() = gold + silver + bronze
}

/** Estilos de natación dominados (de 5) y el que está en proceso. */
@Immutable
data class SwimProgress(val mastered: List<String>, val inProgress: String? = null, val total: Int = 5) {
    val fraction: Float get() = mastered.size / total.toFloat()
}

/**
 * Próximo compromiso que se destaca en la card. [emphasized] = examen o evento clave (franja
 * `tertiaryContainer` con cuenta regresiva); si no, fila neutra con [badge] ("Inscrita").
 */
@Immutable
data class NextCommitment(
    val sport: Sport,
    val title: String,
    val date: LocalDate,
    val place: String? = null,
    val badge: String? = null,
    val emphasized: Boolean = false,
)

@Immutable
data class BeltStep(val belt: Belt, val date: LocalDate)

@Immutable
data class TkdEvent(
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val place: String,
    val costLabel: String,
    val costPesos: Int,
    val payment: PaymentStatus,
)

@Immutable
data class Equipment(val name: String, val detail: String, @DrawableRes val icon: Int, val approved: Boolean = true)

/** Datos de la pestaña Taekwondo (P09). */
@Immutable
data class TkdProfile(
    val belt: Belt,
    val levelLabel: String,
    val instructor: String,
    val school: String,
    val progressToNext: Float,
    val nextEvent: TkdEvent?,
    val fightsWon: Int,
    val fightsTotal: Int,
    val formsLabel: String,
    val attendancePercent: Int,
    val history: List<BeltStep>,
    val equipment: List<Equipment>,
)

/** Hijo con su progreso por deporte. [colorIndex] apunta a `AthleteColors.all`. */
@Immutable
data class AthleteProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val colorIndex: Int,
    val birthDate: LocalDate,
    val sports: List<Sport>,
    val medals: Medals,
    val tutors: Int,
    @DrawableRes val avatarRes: Int? = null,
    @DrawableRes val photoRes: Int? = avatarRes,
    val highlight: String? = null,
    val tkd: TkdProfile? = null,
    val swim: SwimProgress? = null,
    val nextCommitment: NextCommitment? = null,
) {
    val fullName: String get() = "$firstName $lastName"
}

@Composable
@ReadOnlyComposable
internal fun AthleteProfile.color(): Color =
    SportKidTheme.sportColors.athlete.all.let { it[colorIndex.mod(it.size)] }

/**
 * Datos de demostración (Figma "SportKid - Deportistas" y "Perfil de deportista"), con fechas
 * ancladas al mes actual para coincidir con la agenda. TODO: reemplazar por el repositorio.
 */
object AthleteSamples {
    const val FAMILY = "Gómez"

    fun athletes(today: LocalDate = LocalDate.now()): List<AthleteProfile> {
        val month = YearMonth.from(today)
        val exam = month.atDay(14)
        return listOf(
            AthleteProfile(
                id = "mateo",
                firstName = "Mateo",
                lastName = FAMILY,
                colorIndex = 0,
                birthDate = LocalDate.of(2017, 5, 15),
                sports = listOf(Sport.TAEKWONDO, Sport.SWIMMING),
                medals = Medals(gold = 3, silver = 1),
                tutors = 2,
                avatarRes = R.drawable.img_sample_mateo,
                photoRes = R.drawable.img_sample_mateo_profile,
                tkd = TkdProfile(
                    belt = Belt.GREEN,
                    levelLabel = "Nivel Intermedio",
                    instructor = "Profesor Min-Soo Kim",
                    school = "Doyang Central · Federación Mexicana",
                    progressToNext = 0.75f,
                    nextEvent = TkdEvent(
                        title = "Examen oficial de promoción a cinta azul",
                        date = exam,
                        time = LocalTime.of(9, 0),
                        place = "Gimnasio Doyang Central (Pista B)",
                        costLabel = "Cuota federada",
                        costPesos = 650,
                        payment = PaymentStatus.PENDING,
                    ),
                    fightsWon = 14,
                    fightsTotal = 16,
                    formsLabel = "Taegeuk 1-4",
                    attendancePercent = 92,
                    history = listOf(
                        BeltStep(Belt.WHITE, LocalDate.of(2024, 1, 15)),
                        BeltStep(Belt.YELLOW, LocalDate.of(2024, 5, 18)),
                        BeltStep(Belt.GREEN, LocalDate.of(2025, 6, 7)),
                    ),
                    equipment = listOf(
                        Equipment("Uniforme Dobok Oficial", "Talla 130 cm · Cuello blanco", R.drawable.ic_profile_dobok),
                        Equipment("Peto reversible Hogu", "Azul / Rojo · Talla 1", R.drawable.ic_profile_chest_guard),
                        Equipment("Careta protectora WT", "Con careta de mica transparente", com.invex.sportkid.core.ui.R.drawable.ic_sport_tkd),
                    ),
                ),
                swim = SwimProgress(mastered = listOf("Crol", "Dorso", "Pecho")),
                nextCommitment = NextCommitment(Sport.TAEKWONDO, "Examen de cinta", exam, emphasized = true),
            ),
            AthleteProfile(
                id = "sofia",
                firstName = "Sofía",
                lastName = FAMILY,
                colorIndex = 1,
                birthDate = LocalDate.of(2019, 3, 2),
                sports = listOf(Sport.SWIMMING, Sport.ATHLETICS),
                medals = Medals(gold = 2),
                tutors = 2,
                avatarRes = R.drawable.img_sample_sofia,
                highlight = "Debutante del mes",
                swim = SwimProgress(mastered = listOf("Crol", "Dorso", "Pecho", "Mariposa"), inProgress = "Combinado"),
                nextCommitment = NextCommitment(
                    Sport.ATHLETICS,
                    "Próxima carrera 3K infantil",
                    month.atDay(22),
                    place = "Pista Olímpica",
                    badge = "Inscrita",
                ),
            ),
        )
    }
}
