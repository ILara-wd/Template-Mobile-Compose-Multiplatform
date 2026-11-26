package com.invex.one.template.core.designsystem.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize

/** Rounded square (radius 12, soft shadow) holding a glyph at its intrinsic Figma size. */
@Composable
fun IconTile(
    @DrawableRes icon: Int,
    containerColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = AppSize.iconTile,
    shape: Shape = AppTheme.shapes.small,
    elevated: Boolean = true,
) {
    IconTile(containerColor, modifier, size, shape, elevated) { AppIcon(icon) }
}

/** Tile showing short text (initials) when no artwork exists yet. */
@Composable
fun MonogramTile(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = AppSize.iconTile,
) {
    IconTile(containerColor = containerColor, modifier = modifier, size = size) {
        Text(text, style = AppTheme.typography.title, color = contentColor, maxLines = 1)
    }
}

@Composable
fun IconTile(
    containerColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = AppSize.iconTile,
    shape: Shape = AppTheme.shapes.small,
    elevated: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .then(if (elevated) Modifier.appShadow(AppTheme.elevation.xs, shape, AppTheme.colors.shadow) else Modifier)
            .background(containerColor, shape),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
