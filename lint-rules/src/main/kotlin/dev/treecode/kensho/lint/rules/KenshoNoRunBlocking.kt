package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression

/**
 * FF-02: sin `runBlocking` en `src/main` (criterio 5 de `01-governance.md §8`).
 *
 * Bloquea el hilo (ANR si es el principal). En tests se usa `runTest`.
 *
 * ```kotlin
 * val total = runBlocking { repository.total() }               // ❌
 * viewModelScope.launch { _uiState.update { … repository.total() … } } // ✅
 * ```
 */
class KenshoNoRunBlocking(config: Config) : KenshoRule(
    config,
    id = "KenshoNoRunBlocking",
    description = "FF-02: `runBlocking` prohibido; usa suspend + viewModelScope (o runTest en tests).",
    severity = Severity.Performance,
) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text == "runBlocking") {
            report(expression, "FF-02: `runBlocking` bloquea el hilo; convierte a `suspend` y lanza en `viewModelScope`.")
        }
    }
}
