package com.invex.one.template.core.designsystem.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppAlpha
import com.invex.one.template.core.designsystem.token.AppSize

/** Circular avatar with an outer ring (Figma: `0 0 0 2px` spread shadow). */
@Composable
fun AppAvatar(
    @DrawableRes image: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = AppSize.avatar,
    ringColor: Color = AppTheme.colors.primaryContainer.copy(alpha = AppAlpha.Ring),
    ringWidth: Dp = AppSize.avatarRing,
) {
    Image(
        painter = painterResource(image),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .drawBehind {
                drawCircle(ringColor, radius = this.size.minDimension / 2 + ringWidth.toPx())
            }
            .clip(AppTheme.shapes.pill),
    )
}
