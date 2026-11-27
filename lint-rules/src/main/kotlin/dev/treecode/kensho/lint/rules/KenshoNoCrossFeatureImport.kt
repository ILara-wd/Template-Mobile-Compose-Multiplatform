package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.config
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * ARC-05 / FF-08: un feature no importa otro feature (ADR-001).
 *
 * Lo compartido va a `components/`, `model/` o `data/`; la navegación entre features sale por `Actions` hacia
 * `TkdApp`. La lista de features se configura con `features` en `detekt.yml`.
 *
 * ```kotlin
 * // en payments/TkdPaymentsScreen.kt
 * import dev.treecode.kensho.students.TkdStudentsSections   // ❌
 * import dev.treecode.kensho.components.molecules.TkdStudentCard  // ✅
 * ```
 */
class KenshoNoCrossFeatureImport(config: Config) : KenshoRule(
    config,
    id = "KenshoNoCrossFeatureImport",
    description = "ARC-05: un feature no importa otro; mueve lo compartido a components/, model/ o data/ (ADR-001).",
) {

    private val features: List<String> by config(DEFAULT_FEATURES)

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)
        val current = featureOf(importDirective.packageName()) ?: return
        val imported = importDirective.importedFqName?.asString() ?: return
        val target = featureOf(imported) ?: return
        if (target != current) {
            report(
                importDirective,
                "ARC-05: `$current` importa `$imported` del feature `$target`; " +
                    "mueve lo compartido a `components/`, `model/` o `data/`, o navega con `Actions`.",
            )
        }
    }

    private fun featureOf(fqName: String): String? = features.firstOrNull { feature ->
        val prefix = "$KENSHO_PACKAGE.$feature"
        fqName == prefix || fqName.startsWith("$prefix.")
    }

    private companion object {
        val DEFAULT_FEATURES = listOf("login", "signup", "students", "profile", "checkin", "payments", "ui.present")
    }
}
