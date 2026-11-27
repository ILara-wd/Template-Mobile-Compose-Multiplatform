package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * I18N-01 / FF-05: cualquier texto visible vía recursos (ADR-009).
 *
 * Detecta un string literal como texto de `Text(…)` (primer argumento o `text =`) y como `contentDescription`.
 * Los datos de las *Fake* en `data/` no son UI y no pasan por aquí.
 *
 * ```kotlin
 * Text("Pagos")                                          // ❌
 * Text(stringResource(R.string.tkd_payments_title))      // ✅
 * Icon(…, contentDescription = "Regresar")               // ❌
 * ```
 */
class KenshoNoLiteralText(config: Config) : KenshoRule(
    config,
    id = "KenshoNoLiteralText",
    description = "I18N-01: el texto visible sale de strings.xml con stringResource (ADR-009).",
    severity = Severity.Maintainability,
) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val isText = expression.calleeExpression?.text == "Text"
        expression.valueArguments.forEachIndexed { index, argument ->
            val name = argument.getArgumentName()?.asName?.asString()
            val isTextArgument = isText && (name == "text" || (name == null && index == 0))
            if ((isTextArgument || name == "contentDescription") && argument.isVisibleLiteral()) {
                report(argument, "I18N-01: `${argument.text}` es texto literal; muévelo a `strings.xml` (`tkd_<feature>_…`) y usa `stringResource`.")
            }
        }
    }

    /** Literal con contenido; `""` se permite (p. ej. placeholder vacío deliberado). */
    private fun KtValueArgument.isVisibleLiteral(): Boolean {
        val literal = getArgumentExpression() as? KtStringTemplateExpression ?: return false
        return literal.entries.isNotEmpty()
    }
}
