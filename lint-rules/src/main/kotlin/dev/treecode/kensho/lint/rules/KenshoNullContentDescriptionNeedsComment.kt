package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * A11Y-01 / FF-06: `contentDescription = null` solo si es decorativo y con comentario en la misma línea (ADR-008).
 *
 * ```kotlin
 * Icon(painterResource(R.drawable.ic_tkd_trophy), contentDescription = null)                         // ❌
 * Icon(painterResource(R.drawable.ic_tkd_check_small), contentDescription = null) // decorativo: …    // ✅
 * ```
 */
class KenshoNullContentDescriptionNeedsComment(config: Config) : KenshoRule(
    config,
    id = "KenshoNullContentDescriptionNeedsComment",
    description = "A11Y-01: un contentDescription null se justifica con un comentario en la misma línea (ADR-008).",
    severity = Severity.Maintainability,
) {

    override fun visitArgument(argument: KtValueArgument) {
        super.visitArgument(argument)
        if (argument.getArgumentName()?.asName?.asString() != "contentDescription") return
        val value = argument.getArgumentExpression() as? KtConstantExpression ?: return
        if (value.text != "null") return
        if (!lineOf(argument).contains("//")) {
            report(argument, "A11Y-01: `contentDescription = null` sin justificar; agrega `// decorativo: <motivo>` o usa `tkd_cd_*`.")
        }
    }

    private fun lineOf(argument: KtValueArgument): String {
        val text = argument.containingKtFile.text
        val end = argument.textRange.endOffset
        val lineEnd = text.indexOf('\n', end).let { if (it == -1) text.length else it }
        return text.substring(end, lineEnd)
    }
}
