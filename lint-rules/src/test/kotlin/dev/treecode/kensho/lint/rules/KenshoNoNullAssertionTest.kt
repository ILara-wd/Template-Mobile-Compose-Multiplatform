package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoNullAssertionTest {

    private val rule = KenshoNoNullAssertion(Config.empty)

    @Test
    fun doubleBang_isReported() {
        assertThat(rule.lint("fun first(list: List<String>?) = list!!.first()")).hasSize(1)
    }

    @Test
    fun elvisAndRequireNotNull_areAllowed() {
        val code = """
            fun first(list: List<String>?) = list?.firstOrNull() ?: ""
            fun second(list: List<String>?) = requireNotNull(list) { "sin lista" }
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }
}
