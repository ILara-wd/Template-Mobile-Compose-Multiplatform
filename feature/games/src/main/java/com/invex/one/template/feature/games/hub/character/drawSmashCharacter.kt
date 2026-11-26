package com.invex.one.template.feature.games.hub.character

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.invex.one.template.feature.games.hub.CharacterType
import com.invex.one.template.feature.games.hub.SmashEntity

fun DrawScope.drawSmashCharacter(entity: SmashEntity) {
    val bodyColor = when (entity.type) {
        CharacterType.BASIC -> Color(0xFF4CAF50)
        CharacterType.RUNNER -> Color(0xFFFF9800)
        CharacterType.TANK -> Color(0xFF7E57C2)
        CharacterType.DECOY -> Color(0xFF29B6F6)
    }

    val scaleX = if (entity.isSmashed) 1.32f else 1f
    val scaleY = if (entity.isSmashed) 0.42f else 1f
    val alpha = if (entity.isSmashed) 0.55f else 1f

    withTransform({
        translate(left = entity.x, top = entity.y)
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero)
    }) {
        val r = entity.radius

        // Sombra / base del cuerpo
        drawCircle(
            color = bodyColor.copy(alpha = alpha),
            radius = r,
            center = Offset.Zero
        )

        // Detalles distintivos por tipo
        when (entity.type) {
            CharacterType.TANK -> {
                // Borde blindado y barra/anillo de armadura
                drawCircle(
                    color = Color(0xFFD1C4E9).copy(alpha = alpha),
                    radius = r * 0.92f,
                    center = Offset.Zero,
                    style = Stroke(width = 5f)
                )
                // Casco superior
                drawRoundRect(
                    color = Color(0xFF4527A0).copy(alpha = alpha),
                    topLeft = Offset(-r * 0.65f, -r * 0.95f),
                    size = Size(r * 1.3f, r * 0.42f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
            }

            CharacterType.RUNNER -> {
                // Líneas de velocidad / picos laterales
                val spike = Path().apply {
                    moveTo(-r * 0.45f, -r * 0.85f)
                    lineTo(-r * 0.2f, -r * 1.25f)
                    lineTo(0f, -r * 0.85f)
                    lineTo(r * 0.2f, -r * 1.25f)
                    lineTo(r * 0.45f, -r * 0.85f)
                    close()
                }
                drawPath(path = spike, color = Color(0xFFFFE082).copy(alpha = alpha))
            }

            CharacterType.DECOY -> {
                // Halo o señal de "no golpear" (cruz/anillo azul claro)
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f * alpha),
                    radius = r * 0.72f,
                    center = Offset.Zero,
                    style = Stroke(width = 3.5f)
                )
            }

            CharacterType.BASIC -> Unit
        }

        // Ojos y expresión
        val eyeOffsetX = r * 0.34f
        val eyeOffsetY = -r * 0.12f
        val eyeRadius = r * 0.14f

        if (entity.isSmashed) {
            // Ojos en "X" al aplastar
            val crossSize = eyeRadius * 1.1f
            listOf(-eyeOffsetX, eyeOffsetX).forEach { ex ->
                drawLine(
                    color = Color(0xFF1A1A1A).copy(alpha = alpha),
                    start = Offset(ex - crossSize, eyeOffsetY - crossSize),
                    end = Offset(ex + crossSize, eyeOffsetY + crossSize),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF1A1A1A).copy(alpha = alpha),
                    start = Offset(ex + crossSize, eyeOffsetY - crossSize),
                    end = Offset(ex - crossSize, eyeOffsetY + crossSize),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
            }
        } else {
            // Esclerótica blanca
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 1.25f,
                center = Offset(-eyeOffsetX, eyeOffsetY)
            )
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 1.25f,
                center = Offset(eyeOffsetX, eyeOffsetY)
            )

            // Pupila (más pequeña/firme en DECOY, intensa en enemigos)
            val pupilColor = if (entity.type == CharacterType.DECOY) Color(0xFF01579B) else Color(0xFF121212)
            drawCircle(
                color = pupilColor,
                radius = eyeRadius * 0.65f,
                center = Offset(-eyeOffsetX, eyeOffsetY)
            )
            drawCircle(
                color = pupilColor,
                radius = eyeRadius * 0.65f,
                center = Offset(eyeOffsetX, eyeOffsetY)
            )

            // Boca
            if (entity.type == CharacterType.DECOY) {
                // Sonrisa amigable
                drawArc(
                    color = Color.White,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(-r * 0.35f, r * 0.05f),
                    size = Size(r * 0.7f, r * 0.45f),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
            } else {
                // Boca gruñona / tensa
                drawRoundRect(
                    color = Color(0xFF1B1B1B),
                    topLeft = Offset(-r * 0.38f, r * 0.24f),
                    size = Size(r * 0.76f, r * 0.26f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
    }
}
