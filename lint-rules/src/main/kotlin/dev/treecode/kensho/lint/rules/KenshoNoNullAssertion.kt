package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtPostfixExpression

/**
 * FF-02: sin `!!` en `src/main` (criterio 5 de `01-governance.md §8`).
 *
 * ```kotlin
 * val student = students.find { it.id == id }!!                           // ❌
 * val student = students.find { it.id == id } ?: return TkdProfileUiState() // ✅
 * ```
 *
 * Los tests quedan fuera por configuración (`excludes` en `detekt.yml`).
 */
class KenshoNoNullAssertion(config: Config) : KenshoRule(
    config,
    id = "KenshoNoNullAssertion",
    description = "FF-02: `!!` prohibido en src/main; maneja el null con ?:, ?.let o requireNotNull.",
) {

    override fun visitPostfixExpression(expression: KtPostfixExpression) {
        super.visitPostfixExpression(expression)
        if (expression.operationToken == KtTokens.EXCLEXCL) {
            report(expression, "FF-02: `!!` puede cerrar la app; usa `?:`, `?.let { }` o `requireNotNull(…) { \"…\" }`.")
        }
    }
}
