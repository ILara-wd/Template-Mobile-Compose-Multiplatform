package com.invex.one.template.core.designsystem.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/** Soft shadow tinted with the theme shadow color (Figma shadows are low-alpha black). */
fun Modifier.appShadow(elevation: Dp, shape: Shape, color: Color): Modifier =
    shadow(elevation = elevation, shape = shape, clip = false, ambientColor = color, spotColor = color)

/**
 * Hard, unblurred shadow drawn [offsetY] below the shape (Figma `0 3px 0 color`), giving
 * buttons a "pressable" 3D look. Apply before the background.
 */
fun Modifier.depthShadow(offsetY: Dp, color: Color, shape: Shape): Modifier = drawBehind {
    val outline: Outline = shape.createOutline(size, layoutDirection, this)
    translate(top = offsetY.toPx()) { drawOutline(outline, color) }
}
