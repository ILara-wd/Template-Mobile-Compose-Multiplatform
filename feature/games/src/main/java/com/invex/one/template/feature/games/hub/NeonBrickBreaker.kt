package com.invex.one.template.feature.games.hub

// Reemplaza con el nombre de tu paquete:
// package com.tudominio.neonbrickbreaker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class Brick(
    val rect: Rect,
    val color: Color,
    val points: Int = 100
)

///enum class GameStatus {
///    READY,
///    PLAYING,
///    GAME_OVER,
///    VICTORY
///}

private val NeonPalette = listOf(
    Color(0xFFFF2A85), // Rosa neón
    Color(0xFF00F5FF), // Cian neón
    Color(0xFFB829EA), // Morado neón
    Color(0xFF39FF14), // Verde neón
    Color(0xFFFFE600)  // Amarillo neón
)

@Composable
fun NeonBrickBreakerScreen(modifier: Modifier = Modifier) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var status by remember { mutableStateOf(GameStatus.READY) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }

    // Entidades del juego
    var paddleX by remember { mutableFloatStateOf(0f) }
    var ballPos by remember { mutableStateOf(Offset.Zero) }
    var ballVel by remember { mutableStateOf(Offset.Zero) }
    val bricks = remember { mutableStateListOf<Brick>() }

    val paddleWidth = canvasSize.width * 0.26f
    val paddleHeight = 36f
    val paddleY = canvasSize.height - 160f
    val ballRadius = 18f

    fun setupLevel(width: Int, height: Int) {
        if (width == 0 || height == 0) return
        bricks.clear()

        val rows = 5
        val cols = 6
        val padding = 16f
        val topMargin = 180f
        val brickWidth = (width - padding * (cols + 1)) / cols
        val brickHeight = 52f

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = padding + c * (brickWidth + padding)
                val top = topMargin + r * (brickHeight + padding)
                bricks.add(
                    Brick(
                        rect = Rect(left, top, left + brickWidth, top + brickHeight),
                        color = NeonPalette[r % NeonPalette.size],
                        points = (rows - r) * 50
                    )
                )
            }
        }

        val initialPaddleWidth = width * 0.26f
        val initialPaddleY = height - 160f
        paddleX = (width - initialPaddleWidth) / 2f
        ballPos = Offset(width / 2f, initialPaddleY - ballRadius - 10f)
        ballVel = Offset(10f, -12f)
    }

    fun resetBall() {
        paddleX = (canvasSize.width - paddleWidth) / 2f
        ballPos = Offset(canvasSize.width / 2f, paddleY - ballRadius - 10f)
        ballVel = Offset(10f, -12f)
        status = GameStatus.READY
    }

    // Bucle principal sincronizado con la tasa de refresco de pantalla
    LaunchedEffect(status, canvasSize) {
        if (status != GameStatus.PLAYING || canvasSize == IntSize.Zero) return@LaunchedEffect

        var lastTime = withFrameNanos { it }
        while (status == GameStatus.PLAYING) {
            withFrameNanos { frameTime ->
                val dt = ((frameTime - lastTime) / 16_666_667f).coerceIn(0.5f, 2f)
                lastTime = frameTime

                var nextPos = ballPos + (ballVel * dt)

                // Rebote contra paredes laterales y borde superior
                if (nextPos.x - ballRadius <= 0f) {
                    nextPos = nextPos.copy(x = ballRadius)
                    ballVel = ballVel.copy(x = abs(ballVel.x))
                } else if (nextPos.x + ballRadius >= canvasSize.width) {
                    nextPos = nextPos.copy(x = canvasSize.width - ballRadius)
                    ballVel = ballVel.copy(x = -abs(ballVel.x))
                }

                if (nextPos.y - ballRadius <= 110f) {
                    nextPos = nextPos.copy(y = 110f + ballRadius)
                    ballVel = ballVel.copy(y = abs(ballVel.y))
                }

                // Rebote contra la pala con ángulo dinámico
                val paddleRect =
                    Rect(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight)
                if (ballVel.y > 0 &&
                    nextPos.y + ballRadius >= paddleRect.top &&
                    nextPos.y - ballRadius <= paddleRect.bottom &&
                    nextPos.x in (paddleRect.left - ballRadius)..(paddleRect.right + ballRadius)
                ) {
                    val hitFactor = ((nextPos.x - paddleRect.center.x) / (paddleWidth / 2f))
                        .coerceIn(-0.9f, 0.9f)
                    val speed = (ballVel.getDistance() * 1.015f).coerceAtMost(28f)
                    val angle = hitFactor * (Math.PI / 3).toFloat()

                    ballVel = Offset(
                        x = speed * sin(angle),
                        y = -abs(speed * cos(angle))
                    )
                    nextPos = nextPos.copy(y = paddleRect.top - ballRadius)
                }

                // Detección de colisiones con ladrillos
                val hitIndex = bricks.indexOfFirst { brick ->
                    nextPos.x + ballRadius >= brick.rect.left &&
                            nextPos.x - ballRadius <= brick.rect.right &&
                            nextPos.y + ballRadius >= brick.rect.top &&
                            nextPos.y - ballRadius <= brick.rect.bottom
                }

                if (hitIndex != -1) {
                    val hitBrick = bricks.removeAt(hitIndex)
                    score += hitBrick.points

                    val overlapLeft = (nextPos.x + ballRadius) - hitBrick.rect.left
                    val overlapRight = hitBrick.rect.right - (nextPos.x - ballRadius)
                    val overlapTop = (nextPos.y + ballRadius) - hitBrick.rect.top
                    val overlapBottom = hitBrick.rect.bottom - (nextPos.y - ballRadius)
                    val minOverlap = minOf(overlapLeft, overlapRight, overlapTop, overlapBottom)

                    ballVel = if (minOverlap == overlapLeft || minOverlap == overlapRight) {
                        ballVel.copy(x = -ballVel.x)
                    } else {
                        ballVel.copy(y = -ballVel.y)
                    }

                    if (bricks.isEmpty()) {
                        status = GameStatus.VICTORY
                    }
                }

                // Pérdida de vida al caer por debajo de la pantalla
                if (nextPos.y - ballRadius > canvasSize.height) {
                    lives--
                    if (lives <= 0) {
                        status = GameStatus.GAME_OVER
                    } else {
                        resetBall()
                    }
                } else {
                    ballPos = nextPos
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060712))
            .onSizeChanged { newSize ->
                if (canvasSize != newSize) {
                    canvasSize = newSize
                    setupLevel(newSize.width, newSize.height)
                }
            }
            .pointerInput(status) {
                detectTapGestures {
                    when (status) {
                        GameStatus.READY -> status = GameStatus.PLAYING
                        GameStatus.GAME_OVER, GameStatus.VICTORY -> {
                            score = 0
                            lives = 3
                            setupLevel(canvasSize.width, canvasSize.height)
                            status = GameStatus.PLAYING
                        }

                        GameStatus.PLAYING -> Unit
                        else -> {}
                    }
                }
            }
            .pointerInput(canvasSize) {
                detectHorizontalDragGestures { _, dragAmount ->
                    val maxPaddleX = (canvasSize.width - paddleWidth).coerceAtLeast(0f)
                    paddleX = (paddleX + dragAmount).coerceIn(0f, maxPaddleX)
                    if (status == GameStatus.READY) {
                        ballPos = ballPos.copy(x = paddleX + paddleWidth / 2f)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Ladrillos neón
            bricks.forEach { brick ->
                drawNeonRect(brick.rect, brick.color)
            }

            // Pala neón

            val paddleRect = Rect(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight)
            drawNeonRect(paddleRect, Color(0xFF00F5FF), cornerRadius = 18f)
// Pelota con halo brillante
            if (ballPos != Offset.Zero) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFFFF2A85), Color.Transparent),
                        center = ballPos,
                        radius = ballRadius * 2.4f
                    ),
                    radius = ballRadius * 2.4f,
                    center = ballPos
                )
                drawCircle(
                    color = Color.White,
                    radius = ballRadius * 0.75f,
                    center = ballPos
                )
            }
        }
// Marcador superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SCORE: $score",
                color = Color(0xFF00F5FF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "VIDAS: ${"♥ ".repeat(lives.coerceAtLeast(0))}",
                color = Color(0xFFFF2A85),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
// Mensajes de estado
        if (status != GameStatus.PLAYING) {
            val message = when (status) {
                GameStatus.READY -> "TOCA PARA LANZAR"
                GameStatus.GAME_OVER -> "GAME OVER\nToca para reintentar"
                GameStatus.VICTORY -> "¡NIVEL COMPLETADO!\nToca para jugar de nuevo"
                GameStatus.PLAYING -> ""
                else -> ""
            }
            Text(
                text = message,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

private fun DrawScope.drawNeonRect(
    rect: Rect,
    color: Color,
    cornerRadius: Float = 10f
) {
// Halo exterior translúcido
    drawRoundRect(
        color = color.copy(alpha = 0.25f),
        topLeft = Offset(rect.left - 6f, rect.top - 6f),
        size = Size(rect.width + 12f, rect.height + 12f),
        cornerRadius = CornerRadius(cornerRadius + 4f)
    )
// Relleno tenue interior
    drawRoundRect(
        color = color.copy(alpha = 0.18f),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius)
    )
// Borde neón brillante
    drawRoundRect(
        color = color,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius),
        style = Stroke(width = 4f)
    )
}