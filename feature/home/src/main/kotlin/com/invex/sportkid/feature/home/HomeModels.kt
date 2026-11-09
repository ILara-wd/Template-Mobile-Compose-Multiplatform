package com.invex.sportkid.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.PaymentStatus
import com.invex.sportkid.core.model.Sport
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** Hijo en los chips de Inicio. [colorIndex] apunta a `AthleteColors.all`. */
@Immutable
data class HomeAthlete(
    val id: String,
    val firstName: String,
    val colorIndex: Int,
    val birthDate: LocalDate,
    @DrawableRes val photoRes: Int? = null,
)

@Composable
@ReadOnlyComposable
internal fun HomeAthlete.color(): Color =
    SportKidTheme.sportColors.athlete.all.let { it[colorIndex.mod(it.size)] }

/** Compromiso próximo con su costo y estado de pago. */
@Immutable
data class HomeEvent(
    val id: String,
    val sport: Sport,
    val athleteId: String,
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val place: String,
    val costLabel: String,
    val costPesos: Int,
    val payment: PaymentStatus,
)

/** Tipo de logro: define el icono y el estilo de la franja inferior. */
enum class AchievementKind { MEDAL, BELT, RECORD }

/** Logro reciente del carrusel ("5K · 28:41 (Récord)"). */
@Immutable
data class HomeAchievement(
    val id: String,
    val kind: AchievementKind,
    val athleteId: String,
    val title: String,
    val subtitle: String,
    val footer: String,
)

/** Gasto del mes por deporte e hijo (resumen de Inicio). */
@Immutable
data class HomeSpend(val sport: Sport, val athleteId: String, val pesos: Int)

/** Datos de Inicio. */
@Immutable
data class HomeData(
    val athletes: List<HomeAthlete>,
    val events: List<HomeEvent>,
    val achievements: List<HomeAchievement>,
    val spends: List<HomeSpend>,
)

private val EsMx = Locale("es", "MX")

/** 1850 → "1,850". */
internal fun Int.grouped(): String = String.format(EsMx, "%,d", this)

/**
 * Datos de demostración (Figma "SportKid - Inicio"), con fechas relativas a [today] para que
 * siempre haya próximos eventos. TODO: repositorios de eventos, logros y gastos.
 */
object HomeSamples {
    const val MATEO = "mateo"
    const val SOFIA = "sofia"

    fun data(today: LocalDate = LocalDate.now()): HomeData {
        val saturday = today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
        return HomeData(
            athletes = listOf(
                HomeAthlete(MATEO, "Mateo", 0, LocalDate.of(2017, 5, 15), R.drawable.img_sample_home_mateo),
                HomeAthlete(SOFIA, "Sofía", 1, LocalDate.of(2019, 3, 2), R.drawable.img_sample_home_sofia),
            ),
            events = listOf(
                HomeEvent(
                    "exam", Sport.TAEKWONDO, MATEO, "Examen cinta verde", saturday, LocalTime.of(9, 0),
                    "Doyang Central · Polideportivo Norte", "Cuota de examen", 650, PaymentStatus.PENDING,
                ),
                HomeEvent(
                    "swim-meet", Sport.SWIMMING, SOFIA, "Torneo Estatal 100m Libre y Dorso", saturday.plusDays(8), LocalTime.of(8, 30),
                    "Centro Acuático Olímpico", "Inscripción y jueceo", 800, PaymentStatus.PAID,
                ),
                HomeEvent(
                    "race", Sport.ATHLETICS, SOFIA, "Carrera Infantil 3K San Jerónimo", saturday.plusWeeks(3), LocalTime.of(7, 45),
                    "Pista Atlética Estadio Revolución", "Kit de competidor", 400, PaymentStatus.PENDING,
                ),
            ),
            achievements = listOf(
                HomeAchievement("gold-3k", AchievementKind.MEDAL, SOFIA, "3K · 14:04 (Récord)", "Carrera de la Primavera", "Medalla de Oro · Cat. Infantil"),
                HomeAchievement("belt", AchievementKind.BELT, MATEO, "Nueva cinta: Verde", "Taekwondo Doyang Central", "Aprobado con mención técnica"),
                HomeAchievement("pb-50", AchievementKind.RECORD, SOFIA, "Récord personal 50m libre", "Torneo Clasificatorio Club Águilas", "36.42 s (-1.8 s marca anterior)"),
            ),
            spends = listOf(
                HomeSpend(Sport.SWIMMING, SOFIA, 800),
                HomeSpend(Sport.TAEKWONDO, MATEO, 650),
                HomeSpend(Sport.ATHLETICS, SOFIA, 400),
            ),
        )
    }
}
