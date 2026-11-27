package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoGlobalScopeTest {

    private val rule = KenshoNoGlobalScope(Config.empty)

    @Test
    fun globalScopeLaunch_isReported() {
        val code = """
            import kotlinx.coroutines.GlobalScope
            fun sync() { GlobalScope.launch { } }
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(2)
    }

    @Test
    fun viewModelScope_isAllowed() {
        assertThat(rule.lint("fun sync() { viewModelScope.launch { } }")).isEmpty()
    }
}
