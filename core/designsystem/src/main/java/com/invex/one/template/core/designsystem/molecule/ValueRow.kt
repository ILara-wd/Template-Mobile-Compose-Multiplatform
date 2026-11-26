package com.invex.one.template.core.designsystem.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.invex.one.template.core.designsystem.theme.AppTheme

/** Inset box showing a label on the left and a big numeric value on the right ("Récord 4.096 pts"). */
@Composable
fun ValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
) {
    val colors = AppTheme.colors
    val suffixStyle = AppTheme.typography.numericSmall
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, AppTheme.shapes.small)
            .padding(AppTheme.spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = AppTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        Text(
            text = buildAnnotatedString {
                append(value)
                if (suffix != null) {
                    append(" ")
                    withStyle(
                        SpanStyle(
                            fontSize = suffixStyle.fontSize,
                            letterSpacing = suffixStyle.letterSpacing,
                            color = colors.primary,
                        ),
                    ) { append(suffix) }
                }
            },
            style = AppTheme.typography.numeric,
            color = colors.onSurface,
            maxLines = 1,
        )
    }
}
