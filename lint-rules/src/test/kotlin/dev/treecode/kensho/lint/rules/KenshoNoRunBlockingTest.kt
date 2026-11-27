package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoRunBlockingTest {

    private val rule = KenshoNoRunBlocking(Config.empty)

    @Test
    fun runBlocking_isReported() {
        assertThat(rule.lint("fun total() = runBlocking { 1 }")).hasSize(1)
    }

    @Test
    fun runTest_isAllowed() {
        assertThat(rule.lint("fun test() = runTest { }")).isEmpty()
    }
}
