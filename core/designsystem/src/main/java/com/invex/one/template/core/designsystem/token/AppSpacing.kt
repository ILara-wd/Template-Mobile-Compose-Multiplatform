package com.invex.one.template.core.designsystem.token

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale (dp) used by Figma auto-layout paddings and gaps. */
@Immutable
data class AppSpacing(
    val xxxs: Dp = 2.dp,
    val xxs: Dp = 4.dp,
    val xs: Dp = 6.dp,
    val sm: Dp = 8.dp,
    val smd: Dp = 10.dp,
    val mdLg: Dp = 14.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

/** Fixed component dimensions (not part of the spacing rhythm). */
object AppSize {
    val minTouchTarget = 44.dp
    val topBarHeight = 64.dp
    val bottomBarHeight = 64.dp
    val buttonHeight = 44.dp
    val avatar = 32.dp
    val logo = 32.dp
    val iconTile = 40.dp
    val iconTileLarge = 48.dp
    val rankBadge = 44.dp
    val statTileIcon = 32.dp
    val navIndicatorWidth = 56.dp
    val navIndicatorHeight = 32.dp
    val navItemMinWidth = 64.dp
    val progressBarHeight = 8.dp
    val avatarRing = 2.dp
    val filterChipHeight = 36.dp
    val statusDot = 8.dp
    /** Vertical offset of the hard depth shadow under accent buttons. */
    val buttonDepth = 3.dp
}
