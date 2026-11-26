package com.invex.one.template.core.designsystem.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow

/** Elevated white pill action with a leading icon ("Ranking global"). */
@Composable
fun ChipButton(
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = AppTheme.shapes.pill
    Row(
        modifier = modifier
            .appShadow(AppTheme.elevation.xs, shape, AppTheme.colors.shadow)
            .clip(shape)
            .background(AppTheme.colors.surfaceContainerLowest)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.mdLg, vertical = AppTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(icon)
        Text(text, style = AppTheme.typography.label, color = AppTheme.colors.onSurface)
    }
}

@Preview
@Composable
private fun ChipButtonPreview() {
    AppTheme { ChipButton("Ranking global", AppIcons.Globe, onClick = {}, modifier = Modifier.padding(AppTheme.spacing.lg)) }
}
