package com.invex.one.template.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.invex.one.template.R
import com.invex.one.template.core.designsystem.atom.AppAvatar
import com.invex.one.template.core.designsystem.atom.StatChip
import com.invex.one.template.core.designsystem.atom.StatChipSize
import com.invex.one.template.core.designsystem.format.formatScore
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.organism.AppTopBar
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppSize
import com.invex.one.template.data.PlayerSummary

/** App header shared by all tabs: brand, current section, streak, coins and profile. */
@Composable
fun MiniJuegosTopBar(
    section: String,
    player: PlayerSummary,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chipColor = AppTheme.colors.surfaceContainerHigh
    AppTopBar(title = stringResource(R.string.app_title), subtitle = section, modifier = modifier) {
        StatChip(
            icon = AppIcons.Flame,
            text = stringResource(R.string.topbar_streak, player.streakDays),
            containerColor = chipColor,
            size = StatChipSize.Compact,
        )
        StatChip(
            icon = AppIcons.Coin,
            text = formatScore(player.coins),
            containerColor = chipColor,
            size = StatChipSize.Compact,
        )
        Box(
            modifier = Modifier
                .defaultMinSize(AppSize.minTouchTarget, AppSize.minTouchTarget)
                .clip(AppTheme.shapes.pill)
                .clickable(role = Role.Button, onClick = onProfileClick)
                .padding(AppTheme.spacing.xs),
            contentAlignment = Alignment.Center,
        ) {
            AppAvatar(image = player.avatar, contentDescription = stringResource(R.string.profile_content_description))
        }
    }
}
