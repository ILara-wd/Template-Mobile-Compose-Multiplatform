package com.invex.one.template.core.designsystem.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize

/** Rounded square holding a single glyph (40dp, radius 12). */
@Composable
fun IconTile(
    @DrawableRes icon: Int,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    val shape = AppTheme.shapes.small
    Box(
        modifier = modifier
            .size(AppSize.iconTile)
            .appShadow(AppTheme.elevation.xs, shape, AppTheme.colors.shadow)
            .background(containerColor, shape),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(icon, Modifier.size(AppSize.iconTileGlyph))
    }
}
