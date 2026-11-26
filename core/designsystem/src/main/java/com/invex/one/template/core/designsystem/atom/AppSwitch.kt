package com.invex.one.template.core.designsystem.atom

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize
import androidx.compose.ui.unit.dp

/** `Regular`: 48×28 (thumb 20). `Small`: 44×24 (thumb 16). */
enum class SwitchSize { Regular, Small }

/**
 * Pill switch from Figma (primary track, white thumb). Figma only defines the "on" state;
 * "off" uses the neutral high container. Pass `onCheckedChange = null` when a parent row
 * already handles the toggle.
 */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    size: SwitchSize = SwitchSize.Regular,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val (width, height, thumb) = when (size) {
        SwitchSize.Regular -> Triple(AppSize.switchWidth, AppSize.switchHeight, AppSize.switchThumb)
        SwitchSize.Small -> Triple(AppSize.switchWidthSmall, AppSize.switchHeightSmall, AppSize.switchThumbSmall)
    }
    val inset = (height - thumb) / 2
    val thumbOffset by animateDpAsState(if (checked) width - thumb - inset * 2 else 0.dp, label = "thumb")
    Box(
        modifier = modifier
            .padding(AppTheme.spacing.xxs)
            .size(width, height)
            .clip(AppTheme.shapes.pill)
            .background(if (checked) colors.primary else colors.surfaceContainerHighest)
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
                } else {
                    Modifier
                },
            )
            .padding(inset),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = thumbOffset)
                .size(thumb)
                .appShadow(AppTheme.elevation.xs, AppTheme.shapes.pill, colors.shadow)
                .background(colors.surfaceContainerLowest, AppTheme.shapes.pill),
        )
    }
}
