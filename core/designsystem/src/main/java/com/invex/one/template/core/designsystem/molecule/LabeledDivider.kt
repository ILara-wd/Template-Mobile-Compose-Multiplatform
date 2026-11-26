package com.invex.one.template.core.designsystem.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.one.template.core.designsystem.theme.AppTheme

/** Divider with a centered uppercase label ("— O CONTINÚA CON —"). */
@Composable
fun LabeledDivider(
    text: String,
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    lineColor: Color = AppTheme.colors.surfaceContainerHighest,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f).height(thickness).background(lineColor, AppTheme.shapes.pill))
        Text(text.uppercase(), style = AppTheme.typography.overline, color = AppTheme.colors.onSurfaceVariant)
        Box(Modifier.weight(1f).height(thickness).background(lineColor, AppTheme.shapes.pill))
    }
}
