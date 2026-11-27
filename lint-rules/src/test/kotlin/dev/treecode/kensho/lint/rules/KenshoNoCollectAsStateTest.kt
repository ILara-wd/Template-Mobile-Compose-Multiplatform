package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoCollectAsStateTest {

    private val rule = KenshoNoCollectAsState(Config.empty)

    @Test
    fun collectAsState_isReported() {
        assertThat(rule.lint("fun Screen() { val state = viewModel.uiState.collectAsState() }")).hasSize(1)
    }

    @Test
    fun collectAsStateWithLifecycle_isAllowed() {
        assertThat(rule.lint("fun Screen() { val state = viewModel.uiState.collectAsStateWithLifecycle() }")).isEmpty()
    }
}
