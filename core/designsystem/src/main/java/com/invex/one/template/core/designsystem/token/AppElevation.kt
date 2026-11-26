package com.invex.one.template.core.designsystem.token

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shadow levels. Figma: `xs` = 0 1 2 rgba(0,0,0,.05), `md` = 0 4 6 -1 rgba(0,0,0,.1),
 * `bar` = 0 ±1 8 rgba(0,0,0,.04). Rendered with [androidx.compose.ui.draw.shadow].
 */
@Immutable
data class AppElevation(
    val none: Dp = 0.dp,
    val xs: Dp = 1.dp,
    val bar: Dp = 2.dp,
    val md: Dp = 4.dp,
    /** Figma `0 10 15 -3 rgba(0,0,0,.1)` — hero elements. */
    val lg: Dp = 8.dp,
)
