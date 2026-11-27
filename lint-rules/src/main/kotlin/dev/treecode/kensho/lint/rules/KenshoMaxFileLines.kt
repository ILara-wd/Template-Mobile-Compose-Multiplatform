package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.config
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtFile

/**
 * STR-02 / FF-09: archivos de máximo `maxLines` líneas (300 por defecto).
 *
 * `LargeClass` de Detekt no aplica: en Compose un archivo es una lista de funciones, no una clase. Los archivos
 * que ya exceden están en el baseline (TD-KEN-006) y salen al dividirse.
 */
class KenshoMaxFileLines(config: Config) : KenshoRule(
    config,
    id = "KenshoMaxFileLines",
    description = "STR-02: divide el archivo; máximo 300 líneas (TD-KEN-006).",
    severity = Severity.Maintainability,
) {

    private val maxLines: Int by config(DEFAULT_MAX_LINES)

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        val lines = file.text.trimEnd('\n').lines().size
        if (lines > maxLines) {
            report(file, "STR-02: ${file.name} tiene $lines líneas (máx. $maxLines); separa secciones en `Tkd<X>Sections.kt`.")
        }
    }

    private companion object {
        const val DEFAULT_MAX_LINES = 300
    }
}
