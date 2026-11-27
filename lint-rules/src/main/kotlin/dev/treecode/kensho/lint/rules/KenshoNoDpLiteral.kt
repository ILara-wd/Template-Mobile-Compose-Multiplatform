package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

/**
 * THM-02 / FF-04: sin `.dp` / `.sp` literales fuera de `ui/theme/` (ADR-004).
 *
 * ```kotlin
 * Modifier.padding(12.dp)              // ❌
 * Modifier.padding(TkdDimens.Space12)  // ✅
 * ```
 */
class KenshoNoDpLiteral(config: Config) : KenshoRule(
    config,
    id = "KenshoNoDpLiteral",
    description = "THM-02: las medidas salen de TkdDimens y el texto de TkdTypography (ADR-004).",
    severity = Severity.Style,
) {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        if (expression.isInPackage("ui.theme")) return
        val unit = expression.selectorExpression?.text
        if (expression.receiverExpression is KtConstantExpression && unit in UNITS) {
            val hint = if (unit == "dp") "`TkdDimens`" else "`TkdTypography`"
            report(expression, "THM-02: `${expression.text}` es una medida literal; usa un token de $hint.")
        }
    }

    private companion object {
        val UNITS = setOf("dp", "sp")
    }
}
