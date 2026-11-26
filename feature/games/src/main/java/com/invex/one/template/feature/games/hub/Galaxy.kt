package com.invex.one.template.feature.games.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot
import kotlin.random.Random

// Modelos de datos
data class BulletGalaxy(val id: Long, var x: Float, var y: Float)
data class Enemy(val id: Long, var x: Float, var y: Float, val radius: Float = 45f)

@Composable
fun GalaxyDefenderGame(
    modifier: Modifier = Modifier
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    var playerX by remember { mutableFloatStateOf(0f) }
    var bullets by remember { mutableStateOf(listOf<BulletGalaxy>()) }
    var enemies by remember { mutableStateOf(listOf<Enemy>()) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    // Inicializar posición del jugador al conocer el tamaño de pantalla
    LaunchedEffect(screenSize) {
        if (screenSize.width > 0 && playerX == 0f) {
            playerX = screenSize.width / 2f
        }
    }

    // Bucle principal del juego (Game Loop)
    LaunchedEffect(isGameOver, screenSize) {
        if (isGameOver || screenSize == IntSize.Zero) return@LaunchedEffect

        var lastFrameTime = withFrameNanos { it }
        var shootTimer = 0f
        var spawnTimer = 0f

        while (!isGameOver) {
            withFrameNanos { frameTime ->
                val dt = (frameTime - lastFrameTime) / 1_000_000_000f // Segundos transcurridos
                lastFrameTime = frameTime

                // 1. Disparo automático cada 0.35 segundos
                shootTimer += dt
                if (shootTimer >= 0.35f) {
                    shootTimer = 0f
                    bullets = bullets + BulletGalaxy(
                        id = System.nanoTime(),
                        x = playerX,
                        y = screenSize.height - 180f
                    )
                }

                // 2. Generar enemigos cada 1.1 segundos
                spawnTimer += dt
                if (spawnTimer >= 1.1f) {
                    spawnTimer = 0f
                    val randomX = Random.nextFloat() * (screenSize.width - 120f) + 60f
                    enemies = enemies + Enemy(id = System.nanoTime(), x = randomX, y = -50f)
                }

                // 3. Mover balas hacia arriba y enemigos hacia abajo
                val updatedBullets = bullets
                    .map { it.copy(y = it.y - 950f * dt) }
                    .filter { it.y > -20f }

                val updatedEnemies = enemies
                    .map { it.copy(y = it.y + 380f * dt) }

                // 4. Detección de colisiones (Bala vs Enemigo)
                val survivingBullets = mutableListOf<BulletGalaxy>()
                val remainingEnemies = updatedEnemies.toMutableList()

                for (bullet in updatedBullets) {
                    val hitEnemy = remainingEnemies.find { enemy ->
                        hypot(bullet.x - enemy.x, bullet.y - enemy.y) < enemy.radius + 15f
                    }
                    if (hitEnemy != null) {
                        remainingEnemies.remove(hitEnemy)
                        score += 10
                    } else {
                        survivingBullets.add(bullet)
                    }
                }

                // 5. Verificar si un enemigo toca el fondo o al jugador
                val playerY = screenSize.height - 140f
                val crashed = remainingEnemies.any { enemy ->
                    enemy.y > screenSize.height || hypot(playerX - enemy.x, playerY - enemy.y) < enemy.radius + 40f
                }

                if (crashed) {
                    isGameOver = true
                } else {
                    bullets = survivingBullets
                    enemies = remainingEnemies
                }
            }
        }
    }

    // Interfaz y dibujo en Canvas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E17))
            .onSizeChanged { screenSize = it }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    playerX = (playerX + dragAmount.x).coerceIn(50f, screenSize.width - 50f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Dibujar balas
            bullets.forEach { bullet ->
                drawRect(
                    color = Color.Cyan,
                    topLeft = Offset(bullet.x - 6f, bullet.y - 20f),
                    size = Size(12f, 35f)
                )
            }

            // Dibujar enemigos
            enemies.forEach { enemy ->
                drawCircle(
                    color = Color(0xFFFF4D6D),
                    radius = enemy.radius,
                    center = Offset(enemy.x, enemy.y)
                )
            }

            // Dibujar nave del jugador (triángulo)
            val playerY = size.height - 140f
            val shipPath = Path().apply {
                moveTo(playerX, playerY - 45f)
                lineTo(playerX - 40f, playerY + 35f)
                lineTo(playerX + 40f, playerY + 35f)
                close()
            }
            drawPath(path = shipPath, color = Color(0xFF4CC9F0))
        }

        // Marcador
        Text(
            text = "Puntos: $score",
            color = Color.White,
            fontSize = 22.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
        )

        // Pantalla de Game Over
        if (isGameOver) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("GAME OVER", color = Color.Red, fontSize = 36.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    bullets = emptyList()
                    enemies = emptyList()
                    score = 0
                    playerX = screenSize.width / 2f
                    isGameOver = false
                }) {
                    Text("Jugar de nuevo")
                }
            }
        }
    }
}
