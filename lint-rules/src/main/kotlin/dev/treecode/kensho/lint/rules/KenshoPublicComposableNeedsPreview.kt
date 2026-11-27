package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.config
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * CMP-09 / FF-11: cualquier archivo con un `@Composable` público tiene al menos una preview (ADR-010).
 *
 * Cuenta como preview `@Preview` o cualquier multipreview listada en `previewAnnotations` (por defecto
 * `@TkdFontScalePreviews`, A11Y-06). En `detekt.yml` se limita a `components/` como FF-11.
 */
class KenshoPublicComposableNeedsPreview(config: Config) : KenshoRule(
    config,
    id = "KenshoPublicComposableNeedsPreview",
    description = "CMP-09: un componente público necesita al menos una @Preview private en su archivo (ADR-010).",
    severity = Severity.Maintainability,
) {

    private val previewAnnotations: List<String> by config(DEFAULT_PREVIEWS)

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        val functions = file.declarations.filterIsInstance<KtNamedFunction>()
        val publicComposable = functions.firstOrNull { it.isPublicTopLevel() && it.hasAnnotationNamed("Composable") }
            ?: return
        if (functions.none { function -> previewAnnotations.any { function.hasAnnotationNamed(it) } }) {
            report(
                publicComposable,
                "CMP-09: `${publicComposable.name}` no tiene preview en ${file.name}; " +
                    "agrega una `@Preview` `private` (o `@TkdFontScalePreviews` si muestra texto).",
            )
        }
    }

    private fun KtNamedFunction.isPublicTopLevel(): Boolean =
        isTopLevel && !hasModifier(KtTokens.PRIVATE_KEYWORD) && !hasModifier(KtTokens.INTERNAL_KEYWORD)

    private fun KtAnnotated.hasAnnotationNamed(name: String): Boolean =
        annotationEntries.any { it.shortName?.asString() == name }

    private companion object {
        val DEFAULT_PREVIEWS = listOf("Preview", "TkdFontScalePreviews")
    }
}
