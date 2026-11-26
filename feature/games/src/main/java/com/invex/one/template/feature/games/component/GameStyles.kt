package com.invex.one.template.feature.games.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppAlpha
import com.invex.one.template.feature.games.R
import com.invex.one.template.feature.games.model.Game
import com.invex.one.template.feature.games.model.GameCategory

internal data class CategoryStyle(
    @StringRes val label: Int,
    /** Artwork tile background. */
    val tile: Color,
    val tagContainer: Color,
    val tagContent: Color,
)

@Composable
internal fun GameCategory.style(): CategoryStyle {
    val c = AppTheme.colors
    return when (this) {
        GameCategory.Puzzle -> CategoryStyle(
            R.string.games_category_puzzle, c.secondaryFixed,
            c.secondaryContainer.copy(alpha = AppAlpha.TintMedium), c.onSecondaryFixedVariant,
        )
        GameCategory.Reflexes -> CategoryStyle(
            R.string.games_category_reflexes, c.tertiaryFixed,
            c.tertiaryContainer.copy(alpha = AppAlpha.TintStrong), c.onTertiaryFixedVariant,
        )
        GameCategory.Arcade -> CategoryStyle(
            R.string.games_category_arcade, c.primaryFixedDim,
            c.primaryContainer.copy(alpha = AppAlpha.TintMedium), c.onPrimaryFixedVariant,
        )
        GameCategory.Logic -> CategoryStyle(
            R.string.games_category_logic, c.primaryFixed,
            c.primaryContainer.copy(alpha = AppAlpha.Tint), c.onPrimaryFixedVariant,
        )
        GameCategory.Memory -> CategoryStyle(
            R.string.games_category_memory, c.surfaceContainerHighest,
            c.surfaceContainerHigh, c.onSurface,
        )
        GameCategory.Retro -> CategoryStyle(
            R.string.games_category_retro, c.secondaryFixedDim,
            c.secondaryContainer.copy(alpha = AppAlpha.TintMedium), c.onSecondaryFixedVariant,
        )
        GameCategory.Action -> CategoryStyle(
            R.string.games_category_action, c.errorContainer,
            c.errorContainer.copy(alpha = AppAlpha.Overlay), c.onErrorContainer,
        )
    }
}

/** Figma artwork per game; `null` for games that have no design yet. */
@DrawableRes
internal fun Game.artwork(): Int? = when (id) {
    "2048" -> AppIcons.Game2048
    "whack" -> AppIcons.GameWhack
    "maze" -> AppIcons.GameMaze
    "simon" -> AppIcons.GameSimon
    "snake" -> AppIcons.GameSnake
    "pong" -> AppIcons.GamePong
    "invaders" -> AppIcons.GameInvaders
    else -> null
}

/** Up to two initials used as fallback artwork ("Neon Brick Breaker" → "NB", "Galaxy" → "Ga"). */
internal fun Game.monogram(): String {
    val words = name.split(' ', '-').filter { it.isNotBlank() }
    return if (words.size >= 2) {
        "${words[0].first()}${words[1].first()}".uppercase()
    } else {
        name.take(2).replaceFirstChar { it.uppercase() }
    }
}
