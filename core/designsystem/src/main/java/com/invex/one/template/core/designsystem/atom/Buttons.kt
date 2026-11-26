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
import androidx.compose.ui.tooling.preview.Preview
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize

/** Filled pill button (44dp) with optional trailing icon. Fills the available width. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes trailingIcon: Int? = null,
) {
    val shape = AppTheme.shapes.pill
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppSize.buttonHeight)
            .appShadow(AppTheme.elevation.xs, shape, AppTheme.colors.shadow)
            .clip(shape)
            .background(AppTheme.colors.primary)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, style = AppTheme.typography.button, color = AppTheme.colors.onPrimary)
        trailingIcon?.let { AppIcon(it, tint = AppTheme.colors.onPrimary) }
    }
}

/** Low-emphasis text action with a trailing chevron ("Ver historial ›"). */
@Composable
fun TextLinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(AppTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = AppTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, style = AppTheme.typography.label, color = AppTheme.colors.secondary)
        AppIcon(AppIcons.ChevronRight, tint = AppTheme.colors.secondary)
    }
}

@Preview
@Composable
private fun ButtonsPreview() {
    AppTheme {
        Row(Modifier.padding(AppTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            PrimaryButton("Jugar", onClick = {}, trailingIcon = AppIcons.Play, modifier = Modifier.weight(1f))
            TextLinkButton("Ver historial", onClick = {}, modifier = Modifier.padding(start = AppTheme.spacing.lg))
        }
    }
}
