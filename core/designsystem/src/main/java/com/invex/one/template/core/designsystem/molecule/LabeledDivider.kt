package com.invex.one.template.core.designsystem.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.invex.one.template.core.designsystem.theme.AppTheme

/** Hairline divider with a centered uppercase label ("— O CONTINÚA CON —"). */
@Composable
fun LabeledDivider(
    text: String,
    modifier: Modifier = Modifier,
    /** Must match the surface behind, so the label "cuts" the line. */
    backgroundColor: Color = AppTheme.colors.background,
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AppTheme.colors.surfaceContainerHighest),
        )
        Text(
            text.uppercase(),
            style = AppTheme.typography.overline,
            color = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier
                .background(backgroundColor)
                .padding(horizontal = AppTheme.spacing.sm),
        )
    }
}
