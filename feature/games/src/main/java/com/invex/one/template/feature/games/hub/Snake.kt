package com.invex.one.template.feature.games.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private const val SNAKE_GRID_SIZE = 20

private data class GridPoint(val x: Int, val y: Int)

private enum class SnakeDirection(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun isOpposite(other: SnakeDirection): Boolean =
        (this == UP && other == DOWN) ||
            (this == DOWN && other == UP) ||
            (this == LEFT && other == RIGHT) ||
            (this == RIGHT && other == LEFT)
}

@Composable
fun SnakeScreen(
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }

    var snake by remember { mutableStateOf(listOf(GridPoint(10, 10), GridPoint(9, 10), GridPoint(8, 10))) }
    var currentDirection by remember { mutableStateOf(SnakeDirection.RIGHT) }
    var directionQueue by remember { mutableStateOf<List<SnakeDirection>>(emptyList()) }
    var food by remember { mutableStateOf(GridPoint(15, 10)) }

    val haptics = LocalHapticFeedback.current

    fun spawnFood(currentSnake: List<GridPoint>): GridPoint {
        val occupied = currentSnake.toHashSet()
        val freeCells = buildList {
            for (y in 0 until SNAKE_GRID_SIZE) {
                for (x in 0 until SNAKE_GRID_SIZE) {
                    val p = GridPoint(x, y)
                    if (p !in occupied) add(p)
                }
            }
        }
        return if (freeCells.isNotEmpty()) {
            freeCells[Random.nextInt(freeCells.size)]
        } else {
            GridPoint(0, 0)
        }
    }

    fun startNewGame() {
        val initialSnake = listOf(GridPoint(10, 10), GridPoint(9, 10), GridPoint(8, 10))
        snake = initialSnake
        currentDirection = SnakeDirection.RIGHT
        directionQueue = emptyList()
        food = spawnFood(initialSnake)
        score = 0
        isGameOver = false
        isPlaying = true
    }

    fun enqueueDirection(newDir: SnakeDirection) {
        if (!isPlaying) return
        val lastPlanned = directionQueue.lastOrNull() ?: currentDirection
        if (newDir != lastPlanned && !newDir.isOpposite(lastPlanned) && directionQueue.size < 2) {
            directionQueue = directionQueue + newDir
        }
    }

    // Bucle principal de avance por tick
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val stepDelayMs = (165L - (score * 2L)).coerceAtLeast(75L)
            delay(stepDelayMs)
            if (!isPlaying) break

            val nextDir = if (directionQueue.isNotEmpty()) {
                val chosen = directionQueue.first()
                directionQueue = directionQueue.drop(1)
                currentDirection = chosen
                chosen
            } else {
                currentDirection
            }

            val head = snake.first()
            val nextHead = GridPoint(head.x + nextDir.dx, head.y + nextDir.dy)

            // Colisión con bordes
            val hitWall = nextHead.x !in 0 until SNAKE_GRID_SIZE || nextHead.y !in 0 until SNAKE_GRID_SIZE
            val willEat = nextHead == food
            // Si no come, la última cola se libera en este mismo tick
            val bodyToCheck = if (willEat) snake else snake.dropLast(1)
            val hitSelf = nextHead in bodyToCheck

            if (hitWall || hitSelf) {
                isPlaying = false
                isGameOver = true
                if (score > bestScore) bestScore = score
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                break
            }

            val updatedSnake = if (willEat) {
                score += 10
                if (score > bestScore) bestScore = score
                val grown = listOf(nextHead) + snake
                food = spawnFood(grown)
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                grown
            } else {
                listOf(nextHead) + snake.dropLast(1)
            }

            snake = updatedSnake
        }
    }

    var dragAccumulated by remember { mutableStateOf(Offset.Zero) }
    var swipeTriggered by remember { mutableStateOf(false) }
    val swipeThreshold = 36f

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
            SnakeStatCard(label = "PUNTOS", value = score.toString())
            SnakeStatCard(label = "LONGITUD", value = snake.size.toString())
            SnakeStatCard(label = "RÉCORD", value = bestScore.toString())
        }

        val boardBg = Color(0xFF1E293B)
        val gridLineColor = Color.White.copy(alpha = 0.05f)
        val snakeHeadColor = Color(0xFF22C55E)
        val snakeBodyColor = Color(0xFF4ADE80)
        val foodColor = Color(0xFFEF4444)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(boardBg)
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .pointerInput(isPlaying) {
                    detectDragGestures(
                        onDragStart = {
                            dragAccumulated = Offset.Zero
                            swipeTriggered = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (swipeTriggered || !isPlaying) return@detectDragGestures
                            dragAccumulated += dragAmount
                            if (abs(dragAccumulated.x) > swipeThreshold || abs(dragAccumulated.y) > swipeThreshold) {
                                swipeTriggered = true
                                val dir = if (abs(dragAccumulated.x) > abs(dragAccumulated.y)) {
                                    if (dragAccumulated.x > 0) SnakeDirection.RIGHT else SnakeDirection.LEFT
                                } else {
                                    if (dragAccumulated.y > 0) SnakeDirection.DOWN else SnakeDirection.UP
                                }
                                enqueueDirection(dir)
                            }
                        },
                        onDragEnd = {
                            dragAccumulated = Offset.Zero
                            swipeTriggered = false
                        },
                        onDragCancel = {
                            dragAccumulated = Offset.Zero
                            swipeTriggered = false
                        }
                    )
                }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / SNAKE_GRID_SIZE

                // Cuadrícula sutil
                for (i in 1 until SNAKE_GRID_SIZE) {
                    val pos = i * cellSize
                    drawLine(gridLineColor, Offset(pos, 0f), Offset(pos, size.height), strokeWidth = 1f)
                    drawLine(gridLineColor, Offset(0f, pos), Offset(size.width, pos), strokeWidth = 1f)
                }

                // Comida
                drawCircle(
                    color = foodColor,
                    radius = cellSize * 0.38f,
                    center = Offset((food.x + 0.5f) * cellSize, (food.y + 0.5f) * cellSize)
                )

                // Serpiente
                snake.forEachIndexed { index, segment ->
                    val pad = if (index == 0) cellSize * 0.08f else cellSize * 0.14f
                    drawRoundRect(
                        color = if (index == 0) snakeHeadColor else snakeBodyColor,
                        topLeft = Offset(segment.x * cellSize + pad, segment.y * cellSize + pad),
                        size = Size(cellSize - pad * 2f, cellSize - pad * 2f),
                        cornerRadius = CornerRadius(cellSize * 0.25f, cellSize * 0.25f)
                    )
                }
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
                            text = if (isGameOver) "¡Chocaste!" else "Snake",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isGameOver) {
                            Text(
                                text = "Puntuación final: $score",
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
            text = "Desliza en el tablero para cambiar de dirección y comer puntos rojos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SnakeStatCard(label: String, value: String) {
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