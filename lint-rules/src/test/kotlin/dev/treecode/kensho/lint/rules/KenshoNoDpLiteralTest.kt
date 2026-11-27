package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoDpLiteralTest {

    private val rule = KenshoNoDpLiteral(Config.empty)

    @Test
    fun dpAndSp_outsideTheme_areReported() {
        val code = """
            package dev.treecode.kensho.payments
            val padding = 12.dp
            val size = 14.sp
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(2)
    }

    @Test
    fun tokens_areAllowed() {
        val code = """
            package dev.treecode.kensho.payments
            val padding = TkdDimens.Space12
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }

    @Test
    fun literals_insideTheme_areAllowed() {
        val code = """
            package dev.treecode.kensho.ui.theme
            object TkdDimens { val Space12 = 12.dp }
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }
}
