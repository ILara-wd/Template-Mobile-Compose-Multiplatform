package com.invex.one.template.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize

/** Pill with a colored status dot and a short label ("● 7 disponibles"). */
@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    dotColor: Color = AppTheme.colors.primaryContainer,
) {
    val shape = AppTheme.shapes.pill
    Row(
        modifier = modifier
            .appShadow(AppTheme.elevation.xs, shape, AppTheme.colors.shadow)
            .background(AppTheme.colors.surfaceContainerHigh, shape)
            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(AppSize.statusDot).background(dotColor, shape))
        Text(text, style = AppTheme.typography.label, color = AppTheme.colors.onSurface, maxLines = 1)
    }
}
