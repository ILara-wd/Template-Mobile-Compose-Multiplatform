package dev.treecode.kensho.lint.rules

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KenshoPublicComposableNeedsPreviewTest {

    private val rule = KenshoPublicComposableNeedsPreview(Config.empty)

    @Test
    fun publicComposableWithoutPreview_isReported() {
        val code = """
            @Composable
            fun TkdSurface() { }
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(1)
    }

    @Test
    fun previewOrFontScalePreviews_satisfyRule() {
        val withPreview = """
            @Composable
            fun TkdPill() { }
            @Preview
            @Composable
            private fun TkdPillPreview() { TkdPill() }
        """.trimIndent()
        val withFontScale = """
            @Composable
            fun TkdTopBar() { }
            @TkdFontScalePreviews
            @Composable
            private fun TkdTopBarPreview() { TkdTopBar() }
        """.trimIndent()

        assertThat(rule.lint(withPreview)).isEmpty()
        assertThat(rule.lint(withFontScale)).isEmpty()
    }

    @Test
    fun internalComposables_areNotRequired() {
        val code = """
            @Composable
            internal fun PaymentsHeader() { }
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }
}
