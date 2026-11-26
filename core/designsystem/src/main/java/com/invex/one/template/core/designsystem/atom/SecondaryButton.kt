package com.invex.one.template.core.designsystem.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize

/** `Surface`: white with soft shadow. `Tonal`: indigo container, flat. */
enum class SecondaryButtonVariant { Surface, Tonal }

/** Full-width 52dp button (radius 12) with a leading icon — social / alternative actions. */
@Composable
fun SecondaryButton(
    text: String,
    @DrawableRes leadingIcon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SecondaryButtonVariant = SecondaryButtonVariant.Surface,
) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.small
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppSize.fieldHeight)
            .then(
                if (variant == SecondaryButtonVariant.Surface) {
                    Modifier.appShadow(AppTheme.elevation.xs, shape, colors.shadow)
                } else {
                    Modifier
                },
            )
            .clip(shape)
            .background(if (variant == SecondaryButtonVariant.Surface) colors.surfaceContainerLowest else colors.surfaceContainerHigh)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(leadingIcon)
        Text(text, style = AppTheme.typography.subtitle, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
