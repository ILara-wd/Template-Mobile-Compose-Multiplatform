package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoMaxFileLinesTest {

    private val rule = KenshoMaxFileLines(Config.empty)

    @Test
    fun fileOverLimit_isReported() {
        val code = (1..301).joinToString("\n") { "val v$it = $it" }

        assertThat(rule.lint(code)).hasSize(1)
    }

    @Test
    fun fileAtLimit_isAllowed() {
        val code = (1..300).joinToString("\n") { "val v$it = $it" }

        assertThat(rule.lint(code)).isEmpty()
    }
}
