package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoPublicMutableStateFlowTest {

    private val rule = KenshoNoPublicMutableStateFlow(Config.empty)

    @Test
    fun publicMutableFlows_areReported() {
        val code = """
            class TkdPaymentsViewModel {
                val uiState = MutableStateFlow(0)
                val events: MutableSharedFlow<Int> = MutableSharedFlow()
            }
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(2)
    }

    @Test
    fun privateBackingPropertyWithStateFlow_isAllowed() {
        val code = """
            class TkdPaymentsViewModel {
                private val _uiState = MutableStateFlow(0)
                val uiState: StateFlow<Int> = _uiState.asStateFlow()
            }
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }
}
