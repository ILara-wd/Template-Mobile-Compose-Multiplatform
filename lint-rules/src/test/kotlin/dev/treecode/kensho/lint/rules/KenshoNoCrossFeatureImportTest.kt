package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoCrossFeatureImportTest {

    private val rule = KenshoNoCrossFeatureImport(Config.empty)

    @Test
    fun importFromOtherFeature_isReported() {
        val code = """
            package dev.treecode.kensho.payments
            import dev.treecode.kensho.students.TkdStudentsContent
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(1)
    }

    @Test
    fun sharedPackagesAndSameFeature_areAllowed() {
        val code = """
            package dev.treecode.kensho.payments
            import dev.treecode.kensho.payments.TkdPaymentsFormat
            import dev.treecode.kensho.components.molecules.TkdTopBar
            import dev.treecode.kensho.model.TkdPaymentsUiState
            import dev.treecode.kensho.ui.theme.TkdColors
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }

    @Test
    fun overviewInUiPresent_isTreatedAsFeature() {
        val code = """
            package dev.treecode.kensho.ui.present
            import dev.treecode.kensho.checkin.TkdCheckInContent
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(1)
    }

    @Test
    fun appShell_mayImportFeatures() {
        val code = """
            package dev.treecode.kensho.ui
            import dev.treecode.kensho.payments.TkdPaymentsScreen
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }
}
