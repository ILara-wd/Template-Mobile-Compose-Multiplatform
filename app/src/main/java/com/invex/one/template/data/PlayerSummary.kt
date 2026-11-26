package com.invex.one.template.data

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.invex.one.template.R

/** Player data shown in the shared top bar. */
@Immutable
data class PlayerSummary(
    val streakDays: Int,
    val coins: Int,
    @param:DrawableRes val avatar: Int,
)

/** Placeholder player until there is a real session / profile source. */
internal val SamplePlayer = PlayerSummary(streakDays = 5, coins = 1420, avatar = R.drawable.sample_avatar)
