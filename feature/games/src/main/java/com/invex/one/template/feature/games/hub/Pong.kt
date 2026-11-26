package com.invex.one.template.feature.games.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PongScreen(
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }

    // Coordenadas normalizadas (0f..1f) respecto al área del Canvas
    var paddleCenterX by remember { mutableFloatStateOf(0.5f) }
    val paddleWidthNorm = 0.24f
    val paddleHeightNorm = 0.032f
    val paddleYNorm = 0.90f

    var ballX by remember { mutableFloatStateOf(0.5f) }
    var ballY by remember { mutableFloatStateOf(0.35f) }
    val ballRadiusNorm = 0.024f

    // Velocidad en unidades normalizadas por segundo
    var ballVx by remember { mutableFloatStateOf(0.32f) }
    var ballVy by remember { mutableFloatStateOf(0.42f) }
    var ballSpeed by remember { mutableFloatStateOf(0.52f) }

    val haptics = LocalHapticFeedback.current

    fun resetBall(serveTowardsPlayer: Boolean = true) {
        ballX = Random.nextFloat().coerceIn(0.25f, 0.75f)
        ballY = 0.28f
        val angleDeg = if (serveTowardsPlayer) {
            Random.nextFloat() * 50f + 65f // 65°..115° hacia abajo
        } else {
            Random.nextFloat() * 50f - 115f
        }
        val rad = (angleDeg * PI / 180.0).toFloat()
        ballVx = cos(rad) * ballSpeed
        ballVy = sin(rad) * ballSpeed
    }

    fun startNewGame() {
        score = 0
        lives = 3
        paddleCenterX = 0.5f
        ballSpeed = 0.52f
        resetBall(serveTowardsPlayer = true)
        isGameOver = false
        isPlaying = true
    }

    // Bucle de física por frame
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var lastFrameNanos = 0L

        while (isPlaying) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = frameNanos
                    return@withFrameNanos
                }
                val dt = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                lastFrameNanos = frameNanos

                var nextX = ballX + ballVx * dt
                var nextY = ballY + ballVy * dt
                var nextVx = ballVx
                var nextVy = ballVy

                // Rebote pared izquierda / derecha
                if (nextX - ballRadiusNorm < 0f) {
                    nextX = ballRadiusNorm
                    nextVx = -nextVx
                } else if (nextX + ballRadiusNorm > 1f) {
                    nextX = 1f - ballRadiusNorm
                    nextVx = -nextVx
                }

                // Rebote pared superior
                if (nextY - ballRadiusNorm < 0f) {
                    nextY = ballRadiusNorm
                    nextVy = -nextVy
                }

                // Colisión con paleta (solo cuando la pelota baja)
                val paddleTop = paddleYNorm - paddleHeightNorm * 0.5f
                val paddleLeft = paddleCenterX - paddleWidthNorm * 0.5f
                val paddleRight = paddleCenterX + paddleWidthNorm * 0.5f

                if (nextVy > 0f &&
                    nextY + ballRadiusNorm >= paddleTop &&
                    ballY + ballRadiusNorm <= paddleTop + paddleHeightNorm + 0.03f &&
                    nextX + ballRadiusNorm >= paddleLeft &&
                    nextX - ballRadiusNorm <= paddleRight
                ) {
                    nextY = paddleTop - ballRadiusNorm
                    // Offset relativo en [-1f, 1f] según dónde golpeó la paleta
                    val hitOffset = ((nextX - paddleCenterX) / (paddleWidthNorm * 0.5f)).coerceIn(-1f, 1f)
                    val maxBounceAngleRad = (62.0 * PI / 180.0).toFloat()
                    val bounceAngle = hitOffset * maxBounceAngleRad

                    ballSpeed = (ballSpeed + 0.03f).coerceAtMost(1.15f)
                    nextVx = sin(bounceAngle) * ballSpeed
                    nextVy = -cos(bounceAngle) * ballSpeed

                    score += 1
                    if (score > bestScore) bestScore = score
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                // Pelota cae por debajo del borde inferior
                if (nextY - ballRadiusNorm > 1f) {
                    lives -= 1
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (lives <= 0) {
                        isPlaying = false
                        isGameOver = true
                    } else {
                        ballSpeed = (ballSpeed * 0.92f).coerceAtLeast(0.50f)
                        resetBall(serveTowardsPlayer = true)
                        nextX = ballX
                        nextY = ballY
                        nextVx = ballVx
                        nextVy = ballVy
                    }
                }

                ballX = nextX
                ballY = nextY
                ballVx = nextVx
                ballVy = nextVy
            }
        }
    }

    var canvasWidthPx by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PongStatCard(label = "REBOTES", value = score.toString())
            PongStatCard(label = "VIDAS", value = "❤".repeat(lives.coerceAtLeast(0)).ifEmpty { "0" })
            PongStatCard(label = "RÉCORD", value = bestScore.toString())
        }

        val arenaBg = Color(0xFF0F172A)
        val paddleColor = Color(0xFF38BDF8)
        val ballColor = Color(0xFFF8FAFC)
        val guideColor = Color.White.copy(alpha = 0.08f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(arenaBg)
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .pointerInput(isPlaying) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        if (!isPlaying || canvasWidthPx <= 0f) return@detectHorizontalDragGestures
                        val deltaNorm = dragAmount / canvasWidthPx
                        val halfPaddle = paddleWidthNorm * 0.5f
                        paddleCenterX = (paddleCenterX + deltaNorm).coerceIn(halfPaddle, 1f - halfPaddle)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                canvasWidthPx = size.width

                // Línea central de referencia
                drawLine(
                    color = guideColor,
                    start = Offset(0f, size.height * 0.5f),
                    end = Offset(size.width, size.height * 0.5f),
                    strokeWidth = 2f
                )

                // Paleta
                val pWidth = paddleWidthNorm * size.width
                val pHeight = paddleHeightNorm * size.height
                val pLeft = (paddleCenterX - paddleWidthNorm * 0.5f) * size.width
                val pTop = (paddleYNorm - paddleHeightNorm * 0.5f) * size.height

                drawRoundRect(
                    color = paddleColor,
                    topLeft = Offset(pLeft, pTop),
                    size = Size(pWidth, pHeight),
                    cornerRadius = CornerRadius(pHeight * 0.5f, pHeight * 0.5f)
                )

                // Pelota
                val bRadius = ballRadiusNorm * minOf(size.width, size.height)
                drawCircle(
                    color = ballColor,
                    radius = bRadius,
                    center = Offset(ballX * size.width, ballY * size.height)
                )
            }

            if (!isPlaying) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = if (isGameOver) "¡Partida terminada!" else "Pong",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isGameOver) {
                            Text(
                                text = "Rebotes logrados: $score",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { startNewGame() },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (isGameOver) "Jugar de nuevo" else "Iniciar")
                        }
                    }
                }
            }
        }

        Text(
            text = "Arrastra horizontalmente para mover la paleta y devolver la pelota",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PongStatCard(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = Modifier.width(104.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
