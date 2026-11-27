package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoNoFontScaleMultiplicationTest {

    private val rule = KenshoNoFontScaleMultiplication(Config.empty)

    @Test
    fun multiplyingByFontScale_isReported() {
        val code = """
            fun size() = TkdTypography.Headline.fontSize * LocalDensity.current.fontScale
            fun other(density: Density) = density.fontScale * 2f
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(2)
    }

    @Test
    fun toolkitPatterns_areAllowed() {
        val code = """
            fun limited(systemScale: Float, fontSize: TextUnit) = fontSize / systemScale
            fun capped(density: Density) = density.fontScale.coerceAtMost(1.5f)
            fun scaled(value: TextUnit, factor: Float) = value * factor
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }
}
