package com.invex.one.template.feature.auth.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.invex.one.template.core.designsystem.atom.IconCircle
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize
import com.invex.one.template.feature.auth.R

/** Context card: overlapping game avatars + "N Minijuegos Sincronizados". */
@Composable
internal fun SyncedGamesCard(gamesCount: Int, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, AppTheme.shapes.medium)
            .padding(spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatars overlap by 12dp.
        Row(horizontalArrangement = Arrangement.spacedBy(-spacing.md)) {
            listOf(
                AppIcons.GamepadMini to colors.primaryContainer,
                AppIcons.BoltBadge to colors.tertiaryContainer,
                AppIcons.Robot to colors.secondaryContainer,
            ).forEach { (icon, color) ->
                IconCircle(
                    icon = icon,
                    containerColor = color,
                    size = AppSize.avatarStackItem,
                    modifier = Modifier.appShadow(AppTheme.elevation.xs, AppTheme.shapes.pill, colors.shadow),
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                pluralStringResource(R.plurals.login_synced_games, gamesCount, gamesCount),
                style = AppTheme.typography.button,
                color = colors.onSurface,
            )
            Text(stringResource(R.string.login_synced_supporting), style = AppTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}
