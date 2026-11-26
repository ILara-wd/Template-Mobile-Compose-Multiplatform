package com.invex.one.template.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppSize

/** Segmented meter (e.g. password strength): [filled] of [segments] bars use [color]. */
@Composable
fun StrengthMeter(
    filled: Int,
    modifier: Modifier = Modifier,
    segments: Int = 4,
    color: Color = AppTheme.colors.primary,
    trackColor: Color = AppTheme.colors.surfaceContainerHigh,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        repeat(segments) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(AppSize.strengthBarHeight)
                    .background(if (index < filled) color else trackColor, AppTheme.shapes.pill),
            )
        }
    }
}
