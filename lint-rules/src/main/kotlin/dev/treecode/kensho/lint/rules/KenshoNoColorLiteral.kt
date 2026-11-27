package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

/**
 * THM-01 / FF-03: sin colores literales fuera de `ui/theme/` (ADR-004).
 *
 * Detecta `Color(0x…)`, `Color(r, g, b)` y los colores con nombre de Compose (`Color.White`, `Color.Red`…).
 * Se permiten `Color.Unspecified` (iconos multicolor) y `Color.Transparent` (degradados).
 *
 * ```kotlin
 * Text(text, color = Color(0xFF059669))   // ❌
 * Text(text, color = TkdColors.Success)   // ✅
 * ```
 */
class KenshoNoColorLiteral(config: Config) : KenshoRule(
    config,
    id = "KenshoNoColorLiteral",
    description = "THM-01: los colores salen de TkdColors / TkdBeltPalette (ADR-004).",
    severity = Severity.Style,
) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.isInPackage(THEME_PACKAGE)) return
        val isColorConstructor = expression.calleeExpression?.text == "Color" &&
            expression.valueArguments.isNotEmpty() &&
            (expression.parent as? KtDotQualifiedExpression)?.selectorExpression != expression
        if (isColorConstructor) {
            report(expression, "THM-01: `${expression.text}` es un color literal; usa un token de `TkdColors` (por rol).")
        }
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        if (expression.isInPackage(THEME_PACKAGE)) return
        val receiver = expression.receiverExpression.text
        val selector = expression.selectorExpression?.text
        if ((receiver == "Color" || receiver == COMPOSE_COLOR) && selector in NAMED_COLORS) {
            report(expression, "THM-01: `${expression.text}` es un color literal; usa un token de `TkdColors` (por rol).")
        }
    }

    private companion object {
        const val THEME_PACKAGE = "ui.theme"
        const val COMPOSE_COLOR = "androidx.compose.ui.graphics.Color"
        val NAMED_COLORS = setOf(
            "White", "Black", "Red", "Green", "Blue", "Yellow", "Cyan", "Magenta",
            "Gray", "LightGray", "DarkGray",
        )
    }
}
