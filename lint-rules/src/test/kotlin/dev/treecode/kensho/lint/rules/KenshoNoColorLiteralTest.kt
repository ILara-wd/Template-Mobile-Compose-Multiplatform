package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoColorLiteralTest {

    private val rule = KenshoNoColorLiteral(Config.empty)

    @Test
    fun hexAndNamedColors_outsideTheme_areReported() {
        val code = """
            package dev.treecode.kensho.components.atoms
            val badge = Color(0xFF059669)
            val text = Color.White
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(2)
    }

    @Test
    fun unspecifiedTransparentAndTokens_areAllowed() {
        val code = """
            package dev.treecode.kensho.components.atoms
            val tint = Color.Unspecified
            val fade = Color.Transparent
            val token = TkdColors.Success
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }

    @Test
    fun colors_insideTheme_areAllowed() {
        val code = """
            package dev.treecode.kensho.ui.theme
            object TkdColors { val Success = Color(0xFF059669); val Surface = Color.White }
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }
}
