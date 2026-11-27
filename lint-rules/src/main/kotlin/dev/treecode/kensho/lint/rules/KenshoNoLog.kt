package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * LOG-01 / FF-01: sin logging directo en `src/main` (ADR-012).
 *
 * Kensho no tiene logger propio todavía; mientras no exista uno (con guard de release y sin datos personales,
 * LOG-02), no se registra nada. Detecta `import android.util.Log`, `Log.x(…)`, `println`/`print` y
 * `printStackTrace()`.
 *
 * ```kotlin
 * Log.d("Payments", "total=$total")   // ❌
 * println(state)                       // ❌
 * error.printStackTrace()              // ❌
 * ```
 */
class KenshoNoLog(config: Config) : KenshoRule(
    config,
    id = "KenshoNoLog",
    description = "LOG-01: sin logging directo en src/main (ADR-012).",
    severity = Severity.Security,
) {

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)
        if (importDirective.importedFqName?.asString() == ANDROID_LOG) {
            report(importDirective, "LOG-01: no importes `android.util.Log`; quita el log (ADR-012).")
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.text ?: return
        val receiver = (expression.parent as? KtDotQualifiedExpression)
            ?.takeIf { it.selectorExpression == expression }
            ?.receiverExpression
            ?.text
        val isAndroidLog = receiver in LOG_RECEIVERS && callee in LOG_LEVELS
        val isConsole = receiver == null && callee in CONSOLE_CALLS
        if (isAndroidLog || isConsole || callee == "printStackTrace") {
            report(expression, "LOG-01: `${expression.text.take(MAX_SNIPPET)}` escribe en logcat; quítalo (ADR-012).")
        }
    }

    private companion object {
        const val ANDROID_LOG = "android.util.Log"
        const val MAX_SNIPPET = 40
        val LOG_RECEIVERS = setOf("Log", ANDROID_LOG)
        val LOG_LEVELS = setOf("v", "d", "i", "w", "e", "wtf", "println")
        val CONSOLE_CALLS = setOf("println", "print")
    }
}
