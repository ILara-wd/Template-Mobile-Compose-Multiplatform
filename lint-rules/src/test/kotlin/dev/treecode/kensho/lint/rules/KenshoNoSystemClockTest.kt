package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoSystemClockTest {

    private val rule = KenshoNoSystemClock(Config.empty)

    @Test
    fun clockReads_outsideData_areReported() {
        val code = """
            package dev.treecode.kensho.model
            data class TkdCheckInUiState(val date: LocalDate = LocalDate.now())
            fun stamp() = System.currentTimeMillis()
            fun instant() = java.time.Instant.now()
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(3)
    }

    @Test
    fun clockReads_insideData_areAllowed() {
        val code = """
            package dev.treecode.kensho.data
            class FakeTkdCheckInRepository { fun today() = LocalDate.now() }
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }

    @Test
    fun injectedClock_isAllowed() {
        val code = """
            package dev.treecode.kensho.checkin
            fun today(clock: Clock) = LocalDate.now(clock)
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }
}
