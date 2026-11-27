package dev.treecode.kensho.lint

import dev.treecode.kensho.lint.rules.KenshoMaxFileLines
import dev.treecode.kensho.lint.rules.KenshoNoCollectAsState
import dev.treecode.kensho.lint.rules.KenshoNoColorLiteral
import dev.treecode.kensho.lint.rules.KenshoNoCrossFeatureImport
import dev.treecode.kensho.lint.rules.KenshoNoDpLiteral
import dev.treecode.kensho.lint.rules.KenshoNoFontScaleMultiplication
import dev.treecode.kensho.lint.rules.KenshoNoGlobalScope
import dev.treecode.kensho.lint.rules.KenshoNoLiteralText
import dev.treecode.kensho.lint.rules.KenshoNoLog
import dev.treecode.kensho.lint.rules.KenshoNoNullAssertion
import dev.treecode.kensho.lint.rules.KenshoNoPublicMutableStateFlow
import dev.treecode.kensho.lint.rules.KenshoNoRunBlocking
import dev.treecode.kensho.lint.rules.KenshoNoSystemClock
import dev.treecode.kensho.lint.rules.KenshoNullContentDescriptionNeedsComment
import dev.treecode.kensho.lint.rules.KenshoPublicComposableNeedsPreview
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/**
 * Registra las reglas propias de Kensho en Detekt (`ruleSetId = "kensho"`, sección `kensho:` de `detekt.yml`).
 *
 * | Regla | Estándar | Fitness function |
 * |-------|----------|------------------|
 * | `KenshoNoLog` | LOG-01 | FF-01 |
 * | `KenshoNoNullAssertion`, `KenshoNoGlobalScope`, `KenshoNoRunBlocking` | ADR-012 (criterio 5 de `01-governance.md §8`) | FF-02 |
 * | `KenshoNoColorLiteral` | THM-01 | FF-03 |
 * | `KenshoNoDpLiteral` | THM-02 | FF-04 |
 * | `KenshoNoLiteralText` | I18N-01 | FF-05 |
 * | `KenshoNullContentDescriptionNeedsComment` | A11Y-01 | FF-06 |
 * | `KenshoNoSystemClock` | ARC-07 | FF-07 |
 * | `KenshoNoCrossFeatureImport` | ARC-05 | FF-08 |
 * | `KenshoMaxFileLines` | STR-02 | FF-09 |
 * | `KenshoNoCollectAsState` | PERF-02 | FF-10 |
 * | `KenshoPublicComposableNeedsPreview` | CMP-09 | FF-11 |
 * | `KenshoNoFontScaleMultiplication` | A11Y-06 | FF-13 |
 * | `KenshoNoPublicMutableStateFlow` | ARC-02 | — |
 *
 * Catálogo completo y proceso para añadir reglas: `docs/architecture/08-lint-rules.md §4`.
 */
class KenshoRuleSetProvider : RuleSetProvider {

    override val ruleSetId: String = "kensho"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            KenshoNoLog(config),
            KenshoNoNullAssertion(config),
            KenshoNoGlobalScope(config),
            KenshoNoRunBlocking(config),
            KenshoNoColorLiteral(config),
            KenshoNoDpLiteral(config),
            KenshoNoLiteralText(config),
            KenshoNullContentDescriptionNeedsComment(config),
            KenshoNoSystemClock(config),
            KenshoNoCrossFeatureImport(config),
            KenshoMaxFileLines(config),
            KenshoNoCollectAsState(config),
            KenshoPublicComposableNeedsPreview(config),
            KenshoNoFontScaleMultiplication(config),
            KenshoNoPublicMutableStateFlow(config),
        ),
    )
}
