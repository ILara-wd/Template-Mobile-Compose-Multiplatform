package com.invex.one.demo.ui.components.atoms

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.one.demo.ui.theme.CpSurface

val CpCardShape = RoundedCornerShape(8.dp)

/** Superficie blanca con radio 8dp y sombra sutil (drop-shadow 0 1 1 / 5%). */
fun Modifier.cpCard(shape: Shape = CpCardShape): Modifier = this
    .shadow(
        elevation = 1.dp,
        shape = shape,
        ambientColor = Color.Black.copy(alpha = 0.05f),
        spotColor = Color.Black.copy(alpha = 0.05f),
    )
    .background(CpSurface, shape)

/**
 * Icono estático del kit: respeta tamaño y color nativos del asset. [tint]
 * recolorea la misma geometría cuando el diseño la usa en otro color.
 */
@Composable
fun CpIcon(@DrawableRes id: Int, modifier: Modifier = Modifier, tint: Color? = null) {
    Image(
        painter = painterResource(id),
        contentDescription = null,
        modifier = modifier,
        colorFilter = tint?.let { ColorFilter.tint(it) },
    )
}

/** Badge de estado tipo pill (En curso, Completado, 1 Prioritario…). */
@Composable
fun StatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .background(containerColor, shape)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Text(text = text, style = textStyle, color = contentColor, maxLines = 1)
    }
}

/** Punto indicador de estado. */
@Composable
fun StatusDot(color: Color, size: Dp = 8.dp) {
    Box(Modifier.size(size).background(color, CircleShape))
}

/**
 * Botón compacto de texto + icono opcional (Continuar, Ver reporte, Detalles,
 * WhatsApp, Posponer…).
 */
@Composable
fun CpButton(
    text: String,
    onClick: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    height: Dp = 32.dp,
    horizontalPadding: Dp = 10.dp,
    gap: Dp = 4.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    @DrawableRes leadingIcon: Int? = null,
    @DrawableRes trailingIcon: Int? = null,
    shape: Shape = CpCardShape,
) {
    Row(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(containerColor)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.let { CpIcon(it) }
        Text(text = text, style = textStyle, color = contentColor, maxLines = 1)
        trailingIcon?.let { CpIcon(it) }
    }
}
