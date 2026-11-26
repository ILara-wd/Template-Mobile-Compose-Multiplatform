package com.invex.one.template.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppSize

/**
 * Visual 24dp checkbox (radius 8). Not clickable by itself: wrap it in a toggleable row
 * (see [com.invex.one.template.core.designsystem.molecule.CheckboxRow]) so the whole row is the target.
 * Figma only defines the checked look; unchecked uses a neutral fill with an outline.
 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.extraSmall
    Box(
        modifier = modifier
            .size(AppSize.checkbox)
            .then(
                if (checked) {
                    Modifier.background(colors.primary, shape)
                } else {
                    Modifier
                        .background(colors.surfaceContainerLow, shape)
                        .border(AppSize.avatarRing, colors.outlineVariant, shape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) AppIcon(AppIcons.Check)
    }
}
