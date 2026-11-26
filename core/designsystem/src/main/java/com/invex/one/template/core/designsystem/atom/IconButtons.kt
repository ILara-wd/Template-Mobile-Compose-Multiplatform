package com.invex.one.template.core.designsystem.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppSize

/** Square tonal icon button (44dp, radius 12), e.g. "Volver". */
@Composable
fun TonalIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = AppTheme.shapes.small
    Box(
        modifier = modifier
            .size(AppSize.minTouchTarget)
            .clip(shape)
            .background(AppTheme.colors.surfaceContainerLow)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) { AppIcon(icon) }
}

/** Borderless icon button for inline use (36dp, radius 8), e.g. show/hide password. */
@Composable
fun InlineIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(AppSize.fieldIconButton)
            .clip(AppTheme.shapes.extraSmall)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) { AppIcon(icon) }
}

/** Filled circle with a centered glyph (brand marks, avatar stacks, small badges). */
@Composable
fun IconCircle(
    @DrawableRes icon: Int,
    containerColor: Color,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(containerColor, AppTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) { AppIcon(icon) }
}
