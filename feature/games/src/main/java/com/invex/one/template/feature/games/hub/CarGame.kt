package com.invex.one.template.feature.games.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

data class Obstacle(
    val id: Int,
    var xRatio: Float, // Posición horizontal (0f a 1f)
    var y: Float       // Posición vertical en píxeles
)

@Composable
fun RaceCarGame(
    modifier: Modifier = Modifier
) {
    var carXRatio by remember { mutableFloatStateOf(0.5f) }
    var obstacles by remember { mutableStateOf(listOf<Obstacle>()) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    // Bucle principal del juego (Game Loop)
    LaunchedEffect(isGameOver, canvasWidth, canvasHeight) {
        if (isGameOver || canvasWidth == 0f || canvasHeight == 0f) return@LaunchedEffect

        var lastFrameTime = withFrameNanos { it }
        var spawnTimer = 0f
        var obstacleCounter = 0

        while (!isGameOver) {
            val currentTime = withFrameNanos { it }
            val deltaSeconds = (currentTime - lastFrameTime) / 1_000_000_000f
            lastFrameTime = currentTime

            val speed = 650f + (score * 15f) // Aumenta dificultad con el puntaje
            spawnTimer += deltaSeconds

            // Generar nuevos obstáculos cada 1.1 segundos
            val updatedList = obstacles.toMutableList()
            if (spawnTimer >= 1.1f) {
                spawnTimer = 0f
                obstacleCounter++
                updatedList.add(
                    Obstacle(
                        id = obstacleCounter,
                        xRatio = Random.nextFloat().coerceIn(0.15f, 0.85f),
                        y = -120f
                    )
                )
            }

            // Mover obstáculos y limpiar los que salen de pantalla
            val carWidth = 90f
            val carHeight = 150f
            val carX = (carXRatio * canvasWidth) - (carWidth / 2f)
            val carY = canvasHeight - carHeight - 60f
            val carRect = Rect(carX, carY, carX + carWidth, carY + carHeight)

            val nextObstacles = mutableListOf<Obstacle>()
            for (obs in updatedList) {
                val newY = obs.y + speed * deltaSeconds
                val obsX = (obs.xRatio * canvasWidth) - (carWidth / 2f)
                val obsRect = Rect(obsX, newY, obsX + carWidth, newY + carHeight)

                // Detección de colisión
                if (carRect.overlaps(obsRect)) {
                    isGameOver = true
                }

                if (newY < canvasHeight) {
                    nextObstacles.add(obs.copy(y = newY))
                } else {
                    score++
                }
            }
            obstacles = nextObstacles
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2C2C))
            .pointerInput(isGameOver) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    if (!isGameOver && canvasWidth > 0f) {
                        val deltaRatio = dragAmount / canvasWidth
                        carXRatio = (carXRatio + deltaRatio).coerceIn(0.12f, 0.88f)
                    }
                }
            }
    ) {
        // Lienzo de dibujo
        Canvas(modifier = Modifier.fillMaxSize()) {
            canvasWidth = size.width
            canvasHeight = size.height

            val carWidth = 90f
            val carHeight = 150f

            // Líneas de la carretera
            val laneX = size.width / 2f
            for (y in 0 until size.height.toInt() step 140) {
                drawRect(
                    color = Color.White.copy(alpha = 0.5f),
                    topLeft = Offset(laneX - 8f, y.toFloat()),
                    size = Size(16f, 70f)
                )
            }

            // Obstáculos (autos enemigos en rojo)
            obstacles.forEach { obs ->
                val obsX = (obs.xRatio * size.width) - (carWidth / 2f)
                drawRoundRect(
                    color = Color(0xFFE53935),
                    topLeft = Offset(obsX, obs.y),
                    size = Size(carWidth, carHeight),
                    cornerRadius = CornerRadius(20f, 20f)
                )
            }

            // Auto del jugador (en azul)
            val playerX = (carXRatio * size.width) - (carWidth / 2f)
            val playerY = size.height - carHeight - 60f
            drawRoundRect(
                color = Color(0xFF1E88E5),
                topLeft = Offset(playerX, playerY),
                size = Size(carWidth, carHeight),
                cornerRadius = CornerRadius(20f, 20f)
            )
        }

        // Marcador
        Text(
            text = "Puntos: $score",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
        )

        // Pantalla de Game Over
        if (isGameOver) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.8f), shape = MaterialTheme.shapes.medium)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¡Choque!", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Puntaje final: $score", color = Color.LightGray, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    score = 0
                    obstacles = emptyList()
                    carXRatio = 0.5f
                    isGameOver = false
                }) {
                    Text("Reiniciar")
                }
            }
        }
    }
}
