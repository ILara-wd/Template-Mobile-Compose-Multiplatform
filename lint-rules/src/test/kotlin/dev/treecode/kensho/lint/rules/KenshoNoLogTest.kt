package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoLogTest {

    private val rule = KenshoNoLog(Config.empty)

    @Test
    fun androidLog_importAndCall_reportsBoth() {
        val code = """
            import android.util.Log
            fun total(value: Int) { Log.d("Payments", "total=${'$'}value") }
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(2)
    }

    @Test
    fun printlnAndPrintStackTrace_areReported() {
        val code = """
            fun fail(error: Throwable) {
                println(error)
                error.printStackTrace()
            }
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(2)
    }

    @Test
    fun methodNamedLikeLogLevel_onOtherReceiver_isAllowed() {
        val code = """
            fun render(builder: StringBuilder, list: List<Int>) {
                builder.e(1)
                list.forEach { it }
            }
            fun StringBuilder.e(value: Int) = append(value)
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }
}
