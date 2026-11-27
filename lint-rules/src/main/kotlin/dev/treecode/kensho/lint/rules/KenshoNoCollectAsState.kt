package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression

/**
 * PERF-02 / FF-10: el estado del ViewModel se lee con `collectAsStateWithLifecycle`.
 *
 * `collectAsState()` sigue colectando con la app en segundo plano.
 *
 * ```kotlin
 * val state by viewModel.uiState.collectAsState()               // ❌
 * val state by viewModel.uiState.collectAsStateWithLifecycle()  // ✅
 * ```
 */
class KenshoNoCollectAsState(config: Config) : KenshoRule(
    config,
    id = "KenshoNoCollectAsState",
    description = "PERF-02: usa collectAsStateWithLifecycle para leer el estado del ViewModel.",
    severity = Severity.Performance,
) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text == "collectAsState") {
            report(expression, "PERF-02: `collectAsState()` colecta en segundo plano; usa `collectAsStateWithLifecycle()`.")
        }
    }
}
