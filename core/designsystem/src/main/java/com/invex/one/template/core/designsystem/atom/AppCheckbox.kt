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

/** `Regular`: 24dp, radius 8 (settings rows). `Small`: 14dp, radius 3 (inline legal text). */
enum class CheckboxSize { Regular, Small }

/**
 * Visual checkbox. Not clickable by itself: wrap it in a toggleable container
 * (see [com.invex.one.template.core.designsystem.molecule.CheckboxRow]).
 * Figma only defines the checked look; unchecked uses a neutral fill with an outline.
 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    size: CheckboxSize = CheckboxSize.Regular,
) {
    val colors = AppTheme.colors
    val shape = if (size == CheckboxSize.Small) AppTheme.shapes.tiny else AppTheme.shapes.extraSmall
    Box(
        modifier = modifier
            .size(if (size == CheckboxSize.Small) AppSize.checkboxSmall else AppSize.checkbox)
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
        if (checked) {
            AppIcon(
                AppIcons.Check,
                if (size == CheckboxSize.Small) Modifier.size(AppSize.checkboxSmallGlyph) else Modifier,
            )
        }
    }
}
