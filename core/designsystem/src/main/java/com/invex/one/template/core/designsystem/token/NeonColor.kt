package com.invex.one.template.core.designsystem.token

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

data class Brick(
    val rect: Rect,
    val color: Color,
    val points: Int = 100
)

enum class GameStatus { READY, PLAYING, GAME_OVER, VICTORY }

val NeonPalette = listOf(
    Color(0xFFFF2A85), // Rosa neón
    Color(0xFF00F5FF), // Cian neón
    Color(0xFFB829EA), // Morado neón
    Color(0xFF39FF14), // Verde neón
    Color(0xFFFFE600)  // Amarillo neón
)
