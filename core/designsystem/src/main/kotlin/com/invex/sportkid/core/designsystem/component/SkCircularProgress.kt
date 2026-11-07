package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme

/**
 * Progreso circular (README §1.6): anillo de [strokeWidth] que inicia arriba, con contenido
 * centrado ("60%" / "3/5 estilos").
 */
@Composable
fun SkCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val value = progress.coerceIn(0f, 1f)
    Box(
        modifier
            .size(size)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawArc(trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = stroke)
            if (value > 0f) drawArc(color, startAngle = -90f, sweepAngle = 360f * value, useCenter = false, style = stroke)
        }
        content()
    }
}

/**
 * Máscara de tiempo tipo cronómetro para un campo que guarda solo dígitos (máx. 6): se rellena
 * desde la derecha. Con [lastSeparator] = '.' es `mm:ss.cc` ("3885" → "00:38.85"); con ':' es
 * `hh:mm:ss` ("1452" → "00:14:52").
 */
class SkTimeMask(private val lastSeparator: Char) : VisualTransformation {
    private companion object {
        const val Digits = 6
    }

    /** Posición en "mm:ss.cc" del dígito [k] (0..5). */
    private fun slot(k: Int) = k + (if (k >= 2) 1 else 0) + (if (k >= 4) 1 else 0)

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.takeLast(Digits)
        if (digits.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val padded = digits.padStart(Digits, '0')
        val out = "${padded.substring(0, 2)}:${padded.substring(2, 4)}$lastSeparator${padded.substring(4, 6)}"
        val lead = Digits - digits.length
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset >= digits.length) out.length else slot(lead + offset)

            override fun transformedToOriginal(offset: Int): Int {
                val k = (0 until Digits).firstOrNull { slot(it) >= offset } ?: Digits
                return (k - lead).coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}

/** `mm:ss.cc` (natación). */
val SkTimeMaskTransformation: VisualTransformation = SkTimeMask('.')

/** `hh:mm:ss` (atletismo). */
val SkDurationMaskTransformation: VisualTransformation = SkTimeMask(':')

@SkPreviews
@Composable
private fun SkCircularProgressPreview() = SportKidTheme {
    SkCircularProgress(progress = 0.6f, size = 80.dp, modifier = Modifier.padding(16.dp)) {
        Text("60%", style = MaterialTheme.typography.titleMedium)
    }
}
