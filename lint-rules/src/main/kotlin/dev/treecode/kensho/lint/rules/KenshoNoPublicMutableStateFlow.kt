package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtProperty

/**
 * ARC-02: el ViewModel guarda el estado mutable en privado y expone `StateFlow`.
 *
 * ```kotlin
 * val uiState = MutableStateFlow(TkdPaymentsUiState())                         // ❌
 *
 * private val _uiState = MutableStateFlow(TkdPaymentsUiState())                // ✅
 * val uiState: StateFlow<TkdPaymentsUiState> = _uiState.asStateFlow()
 * ```
 */
class KenshoNoPublicMutableStateFlow(config: Config) : KenshoRule(
    config,
    id = "KenshoNoPublicMutableStateFlow",
    description = "ARC-02: MutableStateFlow/MutableSharedFlow privados; expón StateFlow con asStateFlow().",
) {

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        if (property.isLocal || property.isHidden()) return
        val declared = property.typeReference?.text.orEmpty()
        val initializer = property.initializer?.text.orEmpty()
        val exposesMutable = MUTABLE_FLOWS.any { declared.startsWith(it) } ||
            (declared.isEmpty() && MUTABLE_FLOWS.any { initializer.startsWith(it) })
        if (exposesMutable) {
            report(
                property,
                "ARC-02: `${property.name}` expone un flujo mutable; hazlo `private val _${property.name}` " +
                    "y expón `val ${property.name}: StateFlow<…> = _${property.name}.asStateFlow()`.",
            )
        }
    }

    private fun KtProperty.isHidden(): Boolean =
        hasModifier(KtTokens.PRIVATE_KEYWORD) || hasModifier(KtTokens.PROTECTED_KEYWORD) || hasModifier(KtTokens.INTERNAL_KEYWORD)

    private companion object {
        val MUTABLE_FLOWS = listOf("MutableStateFlow", "MutableSharedFlow")
    }
}
