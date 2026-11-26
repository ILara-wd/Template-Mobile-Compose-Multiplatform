package com.invex.one.template.core.designsystem.token

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Corner radius tokens. */
@Immutable
data class AppShapes(
    /** 3dp — small checkboxes. */
    val tiny: Shape = RoundedCornerShape(3.dp),
    /** 8dp — checkboxes, small inline buttons. */
    val extraSmall: Shape = RoundedCornerShape(8.dp),
    /** 12dp — icon tiles, inset value boxes. */
    val small: Shape = RoundedCornerShape(12.dp),
    /** 16dp — banners. */
    val medium: Shape = RoundedCornerShape(16.dp),
    /** 18dp — metric tiles. */
    val mediumLarge: Shape = RoundedCornerShape(18.dp),
    /** 20dp — cards. */
    val large: Shape = RoundedCornerShape(20.dp),
    /** Fully rounded — chips, buttons, badges, avatars. */
    val pill: Shape = CircleShape,
)
