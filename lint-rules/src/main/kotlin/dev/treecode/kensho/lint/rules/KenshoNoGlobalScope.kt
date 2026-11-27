package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression

/**
 * FF-02: sin `GlobalScope` (criterio 5 de `01-governance.md §8`).
 *
 * El trabajo asíncrono vive en `viewModelScope`, que se cancela con la pantalla.
 *
 * ```kotlin
 * GlobalScope.launch { repository.sync() }     // ❌ sobrevive a la pantalla
 * viewModelScope.launch { repository.sync() }  // ✅
 * ```
 */
class KenshoNoGlobalScope(config: Config) : KenshoRule(
    config,
    id = "KenshoNoGlobalScope",
    description = "FF-02: `GlobalScope` prohibido; usa viewModelScope.",
) {

    override fun visitReferenceExpression(expression: KtReferenceExpression) {
        super.visitReferenceExpression(expression)
        if (expression is KtNameReferenceExpression && expression.getReferencedName() == "GlobalScope") {
            report(expression, "FF-02: `GlobalScope` no se cancela con la pantalla; usa `viewModelScope`.")
        }
    }
}
