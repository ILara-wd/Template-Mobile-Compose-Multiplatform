package com.invex.one.template.feature.records.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.feature.records.R
import com.invex.one.template.feature.records.model.RecordCategory
import com.invex.one.template.feature.records.model.RecordUnit

internal data class TagColors(val container: Color, val content: Color)

@StringRes
internal fun RecordCategory.label(): Int = when (this) {
    RecordCategory.Puzzle -> R.string.records_category_puzzle
    RecordCategory.Arcade -> R.string.records_category_arcade
    RecordCategory.Logic -> R.string.records_category_logic
    RecordCategory.Memory -> R.string.records_category_memory
    RecordCategory.Classic -> R.string.records_category_classic
    RecordCategory.Sports -> R.string.records_category_sports
}

@Composable
internal fun RecordCategory.colors(): TagColors {
    val c = AppTheme.colors
    return when (this) {
        RecordCategory.Puzzle, RecordCategory.Memory, RecordCategory.Sports ->
            TagColors(c.secondaryFixed, c.onSecondaryFixedVariant)
        RecordCategory.Arcade -> TagColors(c.tertiaryFixed, c.onTertiaryFixedVariant)
        RecordCategory.Logic, RecordCategory.Classic -> TagColors(c.primaryFixed, c.onPrimaryFixedVariant)
    }
}

@StringRes
internal fun RecordUnit.label(): Int = when (this) {
    RecordUnit.Points -> R.string.records_unit_points
    RecordUnit.Moles -> R.string.records_unit_moles
    RecordUnit.Completed -> R.string.records_unit_completed
    RecordUnit.Round -> R.string.records_unit_sequence
    RecordUnit.Apples -> R.string.records_unit_apples
    RecordUnit.Bounces -> R.string.records_unit_bounces
}

/** Visual treatment of a podium position (1º gold, 2º silver, 3º bronze). */
internal data class PodiumStyle(
    @DrawableRes val medal: Int,
    val badgeContainer: Color,
    val badgeContent: Color,
)

@Composable
internal fun podiumStyle(rank: Int): PodiumStyle? {
    val c = AppTheme.colors
    return when (rank) {
        1 -> PodiumStyle(AppIcons.MedalGold, c.tertiaryContainer, c.onTertiaryContainer)
        2 -> PodiumStyle(AppIcons.MedalSilver, c.surfaceContainerHigh, c.onSurface)
        3 -> PodiumStyle(AppIcons.MedalBronze, c.tertiaryFixed, c.onTertiaryFixedVariant)
        else -> null
    }
}
