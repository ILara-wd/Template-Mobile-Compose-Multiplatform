package com.invex.one.template.core.designsystem.molecule

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.atom.AppSwitch
import com.invex.one.template.core.designsystem.atom.IconTile
import com.invex.one.template.core.designsystem.atom.SwitchSize
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppSize

/** Visual container of a [SettingsRow]; defaults match the settings list rows. */
data class SettingsRowStyle(
    val containerColor: Color,
    val containerShape: Shape,
    val iconContainerColor: Color,
    val iconContainerShape: Shape,
    val switchSize: SwitchSize = SwitchSize.Small,
) {
    companion object {
        @Composable
        fun list() = SettingsRowStyle(
            containerColor = AppTheme.colors.surfaceContainerLow,
            containerShape = AppTheme.shapes.small,
            iconContainerColor = AppTheme.colors.surfaceContainer,
            iconContainerShape = AppTheme.shapes.extraSmall,
        )
    }
}

/** Row with icon tile, title, supporting text and a chevron (navigates). */
@Composable
fun SettingsNavigationRow(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SettingsRowStyle = SettingsRowStyle.list(),
) {
    SettingsRowLayout(
        icon, title, subtitle, style,
        modifier.clip(style.containerShape).clickable(role = Role.Button, onClick = onClick),
    ) { AppIcon(AppIcons.ChevronRightSmall) }
}

/** Row with icon tile, title, supporting text and a switch; the whole row toggles. */
@Composable
fun SettingsSwitchRow(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    style: SettingsRowStyle = SettingsRowStyle.list(),
) {
    SettingsRowLayout(
        icon, title, subtitle, style,
        modifier
            .clip(style.containerShape)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    ) { AppSwitch(checked = checked, onCheckedChange = null, size = style.switchSize) }
}

@Composable
private fun SettingsRowLayout(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    style: SettingsRowStyle,
    modifier: Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(style.containerColor, style.containerShape)
            .padding(AppTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(
            icon = icon,
            containerColor = style.iconContainerColor,
            size = AppSize.settingsIcon,
            shape = style.iconContainerShape,
            elevated = false,
        )
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.typography.subtitle, color = AppTheme.colors.onSurface, maxLines = 1)
            Text(subtitle, style = AppTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant, maxLines = 1)
        }
        trailing()
    }
}
