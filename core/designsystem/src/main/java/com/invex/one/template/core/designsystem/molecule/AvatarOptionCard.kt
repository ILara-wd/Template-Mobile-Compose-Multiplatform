package com.invex.one.template.core.designsystem.molecule

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.invex.one.template.core.designsystem.atom.IconCircle
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize

/**
 * Selectable avatar tile: tinted circle with the artwork and a name. Selected = 2dp primary
 * ring, lighter container, primary name and a check badge on the top-end corner.
 */
@Composable
fun AvatarOptionCard(
    @DrawableRes image: Int,
    name: String,
    circleColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.medium
    Box(modifier) {
        Column(
            modifier = Modifier
                .then(if (selected) Modifier.border(AppSize.avatarRing, colors.primary, shape) else Modifier)
                .clip(shape)
                .background(if (selected) colors.surfaceContainer else colors.surfaceContainerHighest)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(AppTheme.spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                // Matches Figma, which stretches the artwork to the circle.
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(AppSize.avatarOption)
                    .clip(AppTheme.shapes.pill)
                    .background(circleColor),
            )
            Text(
                name,
                style = AppTheme.typography.label,
                color = if (selected) colors.primary else colors.onSurface,
                maxLines = 1,
                modifier = Modifier.padding(top = AppTheme.spacing.xs),
            )
        }
        if (selected) {
            IconCircle(
                icon = AppIcons.CheckSmall,
                containerColor = colors.primary,
                size = AppSize.selectionBadge,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = AppTheme.spacing.xxs, y = -AppTheme.spacing.xxs)
                    .appShadow(AppTheme.elevation.xs, AppTheme.shapes.pill, colors.shadow),
            )
        }
    }
}
