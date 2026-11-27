package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression

/**
 * A11Y-06 / FF-13: nunca multiplicar por `fontScale` (ADR-015).
 *
 * Los estilos de `TkdTypography` están en `sp` y Android ya los escala: multiplicar por `fontScale` escala dos
 * veces. Para limitar el crecimiento se usa `ui/theme/TkdFontScale.kt` (`withFontScale`, `TkdFontScaleLimit`,
 * `shrinkToFit`).
 *
 * ```kotlin
 * fontSize = TkdTypography.Headline.fontSize * LocalDensity.current.fontScale    // ❌ doble escalado
 * style = TkdTypography.Headline.withFontScale(TkdFontScale.Limited)            // ✅
 * ```
 */
class KenshoNoFontScaleMultiplication(config: Config) : KenshoRule(
    config,
    id = "KenshoNoFontScaleMultiplication",
    description = "A11Y-06: sp ya escala con la preferencia del usuario; no multipliques por fontScale (ADR-015).",
) {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        val isTimes = expression.operationToken == KtTokens.MUL || expression.operationToken == KtTokens.MULTEQ
        if (isTimes && (expression.left.readsFontScale() || expression.right.readsFontScale())) {
            report(
                expression,
                "A11Y-06: `${expression.text}` escala el texto dos veces; usa `withFontScale(TkdFontScale.Limited)` " +
                    "o `shrinkToFit()` de `ui/theme/TkdFontScale.kt`.",
            )
        }
    }

    private fun KtExpression?.readsFontScale(): Boolean =
        this != null && text.substringAfterLast('.') == "fontScale"
}
