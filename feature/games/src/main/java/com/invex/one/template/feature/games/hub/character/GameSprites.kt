package com.invex.one.template.feature.games.hub.character

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource

data class GameSprites(
    val player: ImageBitmap? = null,
    val orb: ImageBitmap? = null,
    val shield: ImageBitmap? = null
)

@Composable
fun rememberGameSprites(
    playerResId: Int? = null,
    orbResId: Int? = null,
    shieldResId: Int? = null
): GameSprites {
    val player = playerResId?.let { ImageBitmap.imageResource(it) }
    val orb = orbResId?.let { ImageBitmap.imageResource(it) }
    val shield = shieldResId?.let { ImageBitmap.imageResource(it) }

    return remember(player, orb, shield) {
        GameSprites(player = player, orb = orb, shield = shield)
    }
}
