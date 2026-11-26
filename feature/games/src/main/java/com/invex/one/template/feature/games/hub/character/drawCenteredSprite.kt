package com.invex.one.template.feature.games.hub.character

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

fun DrawScope.drawCenteredSprite(
    sprite: ImageBitmap?,
    centerX: Float,
    centerY: Float,
    diameter: Float,
    fallbackDraw: DrawScope.() -> Unit
) {
    if (sprite == null) {
        fallbackDraw()
        return
    }

    val half = diameter / 2f
    drawImage(
        image = sprite,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(sprite.width, sprite.height),
        dstOffset = IntOffset(
            x = (centerX - half).roundToInt(),
            y = (centerY - half).roundToInt()
        ),
        dstSize = IntSize(
            width = diameter.roundToInt(),
            height = diameter.roundToInt()
        )
    )
}
