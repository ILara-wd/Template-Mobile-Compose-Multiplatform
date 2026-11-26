package com.invex.one.template.core.designsystem.molecule

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize

/** Bottom navigation destination: pill indicator (56×32) + label. */
@Composable
fun RowScope.NavBarItem(
    @DrawableRes icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.pill
    Column(
        modifier = modifier
            .weight(1f)
            .defaultMinSize(AppSize.navItemMinWidth, AppSize.minTouchTarget)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.xxs),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(AppSize.navIndicatorWidth, AppSize.navIndicatorHeight)
                .then(
                    if (selected) {
                        Modifier
                            .appShadow(AppTheme.elevation.xs, shape, colors.shadow)
                            .background(colors.primaryContainer, shape)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(icon, tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant)
        }
        Text(
            label,
            style = AppTheme.typography.label,
            color = if (selected) colors.onSurface else colors.onSurfaceVariant,
        )
    }
}

