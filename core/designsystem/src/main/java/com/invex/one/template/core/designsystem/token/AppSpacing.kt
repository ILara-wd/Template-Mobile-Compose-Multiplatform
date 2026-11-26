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
    val buttonHeightLarge = 56.dp
    val fieldHeight = 52.dp
    val fieldHeightCompact = 48.dp
    val fieldIconButton = 36.dp
    /** Text start inset inside a field that has a leading icon. */
    val fieldTextInset = 44.dp
    /** Text end inset inside a field that has a trailing icon button. */
    val fieldTrailingInset = 48.dp
    val checkbox = 24.dp
    val checkboxSmall = 14.dp
    val checkboxSmallGlyph = 10.dp
    val selectionBadge = 20.dp
    val avatarOption = 62.5.dp
    val strengthBarHeight = 6.dp
    val heroTile = 80.dp
    val avatarLarge = 96.dp
    val iconSquare = 28.dp
    val settingsIcon = 36.dp
    val characterTile = 44.dp
    val dividerHeight = 24.dp
    val switchWidth = 48.dp
    val switchHeight = 28.dp
    val switchThumb = 20.dp
    val switchWidthSmall = 44.dp
    val switchHeightSmall = 24.dp
    val switchThumbSmall = 16.dp
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
    val statusDotSmall = 6.dp
    val brandMark = 24.dp
    val avatarStackItem = 40.dp
    val readableWidth = 320.dp
    /** Vertical offset of the hard depth shadow under accent buttons. */
    val buttonDepth = 3.dp
}
