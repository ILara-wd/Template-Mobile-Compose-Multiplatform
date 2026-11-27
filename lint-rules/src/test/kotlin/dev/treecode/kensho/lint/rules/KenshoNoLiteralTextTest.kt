package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoLiteralTextTest {

    private val rule = KenshoNoLiteralText(Config.empty)

    @Test
    fun literalInText_positionalAndNamed_isReported() {
        val code = """
            fun Screen() {
                Text("Pagos")
                Text(text = "Alumnos", maxLines = 1)
            }
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(2)
    }

    @Test
    fun literalContentDescription_isReported() {
        assertThat(rule.lint("fun Back() { Icon(icon, contentDescription = \"Regresar\") }")).hasSize(1)
    }

    @Test
    fun stringResourceAndOtherCalls_areAllowed() {
        val code = """
            fun Screen(name: String) {
                Text(stringResource(R.string.tkd_payments_title))
                Text(name)
                Text(text = "")
                Student(name = "Ana López")
            }
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }
}
