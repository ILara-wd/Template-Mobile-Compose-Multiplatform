package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.designsystem.theme.metric

/**
 * Barra de progreso pill. [progress] entre 0 y 1. El relleno puede ser un color o un
 * degradado ([brush]): p. ej. de la cinta actual a la siguiente.
 */
@Composable
fun SkLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    brush: Brush? = null,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    height: Dp = 8.dp,
) {
    val value = progress.coerceIn(0f, 1f)
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(MaterialTheme.shapes.full)
            .background(trackColor)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f) },
    ) {
        if (value > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(value)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.full)
                    .background(brush ?: SolidColor(color)),
            )
        }
    }
}

/**
 * M18. KPI compacto: valor en `metric` centrado y etiqueta debajo, sobre
 * `surfaceContainerLow` (Figma: "14 / 16 Combates", "92% Asistencia").
 */
@Composable
fun SkKpiTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary,
) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)
            .padding(SportKidTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            value,
            style = MaterialTheme.typography.metric.copy(lineHeight = MaterialTheme.typography.titleLarge.lineHeight),
            color = valueColor,
            textAlign = TextAlign.Center,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@SkPreviews
@Composable
private fun SkProgressPreview() = SportKidTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SkLinearProgress(progress = 0.6f, color = SportKidTheme.sportColors.swimming)
        SkLinearProgress(
            progress = 0.75f,
            height = 10.dp,
            brush = Brush.horizontalGradient(listOf(SportKidTheme.sportColors.belt.green, SportKidTheme.sportColors.belt.blue)),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkKpiTile("14 / 16", "Combates (W/L)", Modifier.weight(1f))
            SkKpiTile("92%", "Asistencia mes", Modifier.weight(1f), valueColor = SportKidTheme.sportColors.success)
        }
    }
}
