package com.invex.one.template.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppSize

/** Linear determinate progress with fully rounded track and indicator. */
@Composable
fun AppProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = AppTheme.colors.surfaceContainerLowest,
    indicatorColor: Color = AppTheme.colors.primary,
) {
    val value = progress.coerceIn(0f, 1f)
    val shape = AppTheme.shapes.pill
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppSize.progressBarHeight)
            .clip(shape)
            .background(trackColor)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f) },
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(value)
                .background(indicatorColor, shape),
        )
    }
}
