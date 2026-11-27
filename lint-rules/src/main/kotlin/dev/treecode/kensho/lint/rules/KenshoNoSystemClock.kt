package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

/**
 * ARC-07 / FF-07: tiempo determinista (ADR-006).
 *
 * "Hoy" llega del repositorio (`closingDate()`, `asOf`) o de un `Clock` inyectado al ViewModel, para que las
 * previews y los tests no cambien con el día. Solo `data/` (los repositorios) puede leer el reloj.
 *
 * ```kotlin
 * data class TkdCheckInUiState(val date: LocalDate = LocalDate.now())   // ❌
 * TkdCheckInUiState(date = repository.today())                          // ✅
 * ```
 */
class KenshoNoSystemClock(config: Config) : KenshoRule(
    config,
    id = "KenshoNoSystemClock",
    description = "ARC-07: el reloj del sistema solo se lee en data/; la fecha de referencia viene del repositorio (ADR-006).",
) {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        if (expression.isInPackage("data")) return
        val call = expression.selectorExpression as? KtCallExpression ?: return
        val receiver = expression.receiverExpression.text.substringAfterLast('.')
        val method = call.calleeExpression?.text
        val readsClock = (receiver in TIME_TYPES && method == "now" && call.valueArguments.isEmpty()) ||
            (receiver == "System" && method == "currentTimeMillis")
        if (readsClock) {
            report(expression, "ARC-07: `${expression.text}` hace la UI no determinista; recibe la fecha del repositorio o de un `Clock`.")
        }
    }

    private companion object {
        val TIME_TYPES = setOf("LocalDate", "LocalDateTime", "LocalTime", "ZonedDateTime", "OffsetDateTime", "Instant")
    }
}
