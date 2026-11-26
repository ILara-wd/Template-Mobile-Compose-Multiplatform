package com.invex.one.template.feature.home.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.feature.home.R
import com.invex.one.template.feature.home.model.GameCategory

internal data class GameCategoryStyle(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val container: Color,
    val content: Color,
)

@Composable
internal fun GameCategory.style(): GameCategoryStyle {
    val c = AppTheme.colors
    return when (this) {
        GameCategory.NumberPuzzle -> GameCategoryStyle(R.string.home_category_number_puzzle, AppIcons.DotsGrid, c.tertiaryFixed, c.onTertiaryFixed)
        GameCategory.Reflexes -> GameCategoryStyle(R.string.home_category_reflexes, AppIcons.Reflex, c.primaryFixed, c.onPrimaryFixed)
        GameCategory.Arcade -> GameCategoryStyle(R.string.home_category_arcade, AppIcons.Snake, c.secondaryFixed, c.onSecondaryFixed)
    }
}
