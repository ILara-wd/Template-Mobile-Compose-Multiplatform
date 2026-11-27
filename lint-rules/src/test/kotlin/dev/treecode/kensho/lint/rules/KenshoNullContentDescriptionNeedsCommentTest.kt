package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNullContentDescriptionNeedsCommentTest {

    private val rule = KenshoNullContentDescriptionNeedsComment(Config.empty)

    @Test
    fun nullWithoutComment_isReported() {
        assertThat(rule.lint("fun Trophy() { Icon(icon, contentDescription = null) }")).hasSize(1)
    }

    @Test
    fun nullWithCommentOnSameLine_isAllowed() {
        val code = """
            fun Check() {
                Icon(icon, contentDescription = null) // decorativo: el texto "Al día" ya lo dice
                Icon(
                    icon,
                    contentDescription = null, // decorativo: acompaña al título
                )
            }
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }

    @Test
    fun resourceDescription_isAllowed() {
        assertThat(rule.lint("fun Back() { Icon(icon, contentDescription = stringResource(R.string.tkd_cd_go_back)) }")).isEmpty()
    }
}
