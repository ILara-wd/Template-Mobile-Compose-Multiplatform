package com.invex.sportkid.feature.expenses

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.PaymentStatus
import com.invex.sportkid.core.model.Sport
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

/** Hijo en los filtros de Gastos. [colorIndex] apunta a `AthleteColors.all`. */
@Immutable
data class ExpenseAthlete(val id: String, val firstName: String, val colorIndex: Int)

@Composable
@ReadOnlyComposable
internal fun ExpenseAthlete.color(): Color =
    SportKidTheme.sportColors.athlete.all.let { it[colorIndex.mod(it.size)] }

/** Tipo de gasto; agrupa el detalle del desglose por deporte ("Mensualidad y torneo"). */
enum class ExpenseCategory(@StringRes val labelRes: Int) {
    MONTHLY(R.string.expenses_category_monthly),
    TOURNAMENT(R.string.expenses_category_tournament),
    EXAM(R.string.expenses_category_exam),
    RACE(R.string.expenses_category_race),
}

/**
 * Un gasto del historial. [athleteIds] admite varios hijos (cuota compartida). [icon] reemplaza
 * al icono del deporte cuando el concepto tiene uno propio (examen, abono de alberca).
 */
@Immutable
data class Expense(
    val id: String,
    val date: LocalDate,
    val title: String,
    val concept: String,
    val sport: Sport,
    val athleteIds: List<String>,
    val category: ExpenseCategory,
    val amountPesos: Int,
    val status: PaymentStatus,
    @DrawableRes val icon: Int? = null,
)

/** Foto destacada del mes ("Mateo · Doyang"). */
@Immutable
data class ExpenseMoment(val athleteId: String, val place: String, @DrawableRes val photoRes: Int)

private val EsMx = Locale("es", "MX")

/** 4350 → "4,350". */
internal fun Int.grouped(): String = String.format(EsMx, "%,d", this)

/**
 * Datos de demostración (Figma "SportKid - Gastos y finanzas deportivas"), anclados al mes que se
 * pasa para que siempre haya contenido en el mes actual. TODO: repositorio de gastos.
 */
object ExpensesSamples {
    const val MATEO = "mateo"
    const val SOFIA = "sofia"

    val athletes = listOf(ExpenseAthlete(MATEO, "Mateo", 0), ExpenseAthlete(SOFIA, "Sofía", 1))

    val moments = listOf(
        ExpenseMoment(MATEO, "Doyang", R.drawable.img_sample_moment_mateo),
        ExpenseMoment(SOFIA, "Carrera 3K", R.drawable.img_sample_moment_sofia),
    )

    /** Solo el mes de [today] tiene gastos; los demás muestran el estado vacío. */
    fun expenses(month: YearMonth, today: LocalDate = LocalDate.now()): List<Expense> {
        if (month != YearMonth.from(today)) return emptyList()
        fun day(d: Int) = month.atDay(d.coerceAtMost(month.lengthOfMonth()))
        return listOf(
            Expense("race-3k", day(22), "Carrera Infantil 3K CODE", "Cuota de inscripción", Sport.ATHLETICS, listOf(SOFIA), ExpenseCategory.RACE, 350, PaymentStatus.PAID),
            Expense("swim-state", day(22), "Torneo Estatal 100m Libres", "Inscripción oficial y hit", Sport.SWIMMING, listOf(MATEO), ExpenseCategory.TOURNAMENT, 800, PaymentStatus.PAID),
            Expense("tkd-exam", day(14), "Examen oficial a Cinta Azul", "Cuota federada y cinta", Sport.TAEKWONDO, listOf(MATEO), ExpenseCategory.EXAM, 650, PaymentStatus.PENDING, R.drawable.ic_expenses_exam),
            Expense("tkd-monthly", day(14), "Mensualidad Doyang Central", "Entrenamientos del mes", Sport.TAEKWONDO, listOf(MATEO), ExpenseCategory.MONTHLY, 750, PaymentStatus.PAID),
            Expense("swim-club", day(5), "Abono Club Acuático Olímpico", "Cuota deportiva mensual", Sport.SWIMMING, listOf(MATEO, SOFIA), ExpenseCategory.MONTHLY, 1800, PaymentStatus.PAID, R.drawable.ic_expenses_pool),
        )
    }
}
