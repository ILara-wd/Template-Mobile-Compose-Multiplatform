package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtElement

/** Paquete raíz de la app; las reglas razonan por paquete (no por ruta) para poder probarse sin archivos. */
internal const val KENSHO_PACKAGE = "dev.treecode.kensho"

/**
 * Base de las reglas `Kensho*`: fija el [Issue] y un `report` corto.
 *
 * Cada mensaje empieza con el ID de la regla de `03-standards.md` (p. ej. `THM-01:`) y propone la corrección,
 * para que el hallazgo enseñe sin abrir la documentación (`08-lint-rules.md §1`).
 */
abstract class KenshoRule(
    config: Config,
    id: String,
    description: String,
    severity: Severity = Severity.Defect,
    debt: Debt = Debt.FIVE_MINS,
) : Rule(config) {

    final override val issue = Issue(id, severity, description, debt)

    protected fun report(element: KtElement, message: String) {
        report(CodeSmell(issue, Entity.from(element), message))
    }
}

/** Paquete del archivo que contiene al elemento (`dev.treecode.kensho.payments`). */
internal fun KtElement.packageName(): String = containingKtFile.packageFqName.asString()

/** `true` si el elemento vive en [subPackage] de la app o en uno de sus subpaquetes. */
internal fun KtElement.isInPackage(subPackage: String): Boolean {
    val target = "$KENSHO_PACKAGE.$subPackage"
    val current = packageName()
    return current == target || current.startsWith("$target.")
}
