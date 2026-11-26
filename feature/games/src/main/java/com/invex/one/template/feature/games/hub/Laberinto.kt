package com.invex.one.template.feature.games.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private enum class MazeDifficulty(
    val label: String,
    val cellsPerSide: Int,
    val extraBraidsRatio: Float, // 0f = laberinto perfecto, >0f = añade atajos/bucles
    val playerRadiusRatio: Float
) {
    EASY(
        label = "Fácil",
        cellsPerSide = 7,
        extraBraidsRatio = 0.20f,
        playerRadiusRatio = 0.24f
    ),
    NORMAL(
        label = "Normal",
        cellsPerSide = 11,
        extraBraidsRatio = 0.08f,
        playerRadiusRatio = 0.21f
    ),
    HARD(
        label = "Difícil",
        cellsPerSide = 15,
        extraBraidsRatio = 0.0f,
        playerRadiusRatio = 0.18f
    )
}

private data class MazeCell(
    val row: Int,
    val col: Int,
    var topWall: Boolean = true,
    var rightWall: Boolean = true,
    var bottomWall: Boolean = true,
    var leftWall: Boolean = true,
    var visited: Boolean = false
)

private data class MazeGrid(
    val rows: Int,
    val cols: Int,
    val cells: Array<Array<MazeCell>>
) {
    fun cellAt(r: Int, c: Int): MazeCell? =
        if (r in 0 until rows && c in 0 until cols) cells[r][c] else null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MazeGrid
        return rows == other.rows && cols == other.cols && cells.contentDeepEquals(other.cells)
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + cells.contentDeepHashCode()
        return result
    }
}

enum class GameStatus {
    READY,
    PLAYING,
    CRASHED,
    DROPPED,
    WON,
    GAME_OVER,
    VICTORY
}

@Composable
fun MazeDragScreen(
    modifier: Modifier = Modifier
) {
    var difficulty by remember { mutableStateOf(MazeDifficulty.NORMAL) }
    var maze by remember(difficulty) { mutableStateOf(generateMaze(difficulty)) }
    var status by remember(difficulty) { mutableStateOf(GameStatus.READY) }

    var wins by remember { mutableIntStateOf(0) }
    var attempts by remember { mutableIntStateOf(0) }

    // Posición normalizada (0f..1f dentro del lienzo del laberinto)
    val startNormalized = remember(maze) {
        Offset(
            x = (0.5f / maze.cols),
            y = (0.5f / maze.rows)
        )
    }
    val goalNormalized = remember(maze) {
        Offset(
            x = ((maze.cols - 0.5f) / maze.cols),
            y = ((maze.rows - 0.5f) / maze.rows)
        )
    }

    var playerPosNorm by remember(maze) { mutableStateOf(startNormalized) }
    var trailPoints by remember(maze) { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val haptics = LocalHapticFeedback.current

    fun resetCurrentMaze(newStatus: GameStatus = GameStatus.READY) {
        playerPosNorm = startNormalized
        trailPoints = emptyList()
        status = newStatus
    }

    fun generateNewMaze() {
        maze = generateMaze(difficulty)
        playerPosNorm = Offset(0.5f / maze.cols, 0.5f / maze.rows)
        trailPoints = emptyList()
        status = GameStatus.READY
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabecera de estadísticas y selector de dificultad
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MazeStatPill(label = "NIVEL", value = difficulty.label)
                MazeStatPill(label = "COMPLETADOS", value = wins.toString())
                MazeStatPill(label = "INTENTOS", value = attempts.toString())
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MazeDifficulty.entries.forEach { option ->
                    val isSelected = option == difficulty
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { difficulty = option }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Área del laberinto
        val wallColor = MaterialTheme.colorScheme.onSurface
        val trailColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        val playerColor = MaterialTheme.colorScheme.primary
        val startColor = Color(0xFF2E7D32)
        val goalColor = Color(0xFFD84315)
        val boardBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(boardBgColor)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(maze, status) {
                        detectDragGestures(
                            onDragStart = { startTouch ->
                                if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@detectDragGestures
                                if (status == GameStatus.WON) return@detectDragGestures

                                val touchNorm = Offset(
                                    x = (startTouch.x / canvasSize.width).coerceIn(0f, 1f),
                                    y = (startTouch.y / canvasSize.height).coerceIn(0f, 1f)
                                )
                                val startPx = Offset(
                                    x = startNormalized.x * canvasSize.width,
                                    y = startNormalized.y * canvasSize.height
                                )
                                val grabTolerancePx = min(canvasSize.width, canvasSize.height) / maze.cols * 0.65f
                                val distToStart = hypot(startTouch.x - startPx.x, startTouch.y - startPx.y)

                                if (status == GameStatus.READY || status == GameStatus.CRASHED || status == GameStatus.DROPPED) {
                                    if (distToStart <= grabTolerancePx) {
                                        attempts++
                                        playerPosNorm = startNormalized
                                        trailPoints = listOf(startNormalized)
                                        status = GameStatus.PLAYING
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                } else if (status == GameStatus.PLAYING) {
                                    val currentPx = Offset(
                                        x = playerPosNorm.x * canvasSize.width,
                                        y = playerPosNorm.y * canvasSize.height
                                    )
                                    val distToPlayer = hypot(startTouch.x - currentPx.x, startTouch.y - currentPx.y)
                                    if (distToPlayer <= grabTolerancePx) {
                                        trailPoints = trailPoints + touchNorm
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                if (status != GameStatus.PLAYING) return@detectDragGestures
                                if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@detectDragGestures
                                change.consume()

                                val cellW = canvasSize.width / maze.cols
                                val cellH = canvasSize.height / maze.rows
                                val cellMin = min(cellW, cellH)
                                val radiusPx = cellMin * difficulty.playerRadiusRatio
                                val wallThicknessPx = max(3f, cellMin * 0.08f)

                                val currentPx = Offset(
                                    x = playerPosNorm.x * canvasSize.width,
                                    y = playerPosNorm.y * canvasSize.height
                                )
                                val targetPx = Offset(
                                    x = (currentPx.x + dragAmount.x).coerceIn(radiusPx, canvasSize.width - radiusPx),
                                    y = (currentPx.y + dragAmount.y).coerceIn(radiusPx, canvasSize.height - radiusPx)
                                )

                                // Subdividir el movimiento para no atravesar muros en arrastres rápidos
                                val totalDist = hypot(targetPx.x - currentPx.x, targetPx.y - currentPx.y)
                                val steps = max(1, (totalDist / (cellMin * 0.15f)).toInt())

                                var safePx = currentPx
                                var hitWall = false

                                for (step in 1..steps) {
                                    val t = step.toFloat() / steps.toFloat()
                                    val candidatePx = Offset(
                                        x = currentPx.x + (targetPx.x - currentPx.x) * t,
                                        y = currentPx.y + (targetPx.y - currentPx.y) * t
                                    )

                                    if (collidesWithMazeWall(
                                            centerPx = candidatePx,
                                            radiusPx = radiusPx,
                                            wallThicknessPx = wallThicknessPx,
                                            maze = maze,
                                            cellW = cellW,
                                            cellH = cellH
                                        )
                                    ) {
                                        hitWall = true
                                        break
                                    } else {
                                        safePx = candidatePx
                                    }
                                }

                                val nextNorm = Offset(
                                    x = (safePx.x / canvasSize.width).coerceIn(0f, 1f),
                                    y = (safePx.y / canvasSize.height).coerceIn(0f, 1f)
                                )
                                playerPosNorm = nextNorm
                                trailPoints = trailPoints + nextNorm

                                if (hitWall) {
                                    status = GameStatus.CRASHED
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    return@detectDragGestures
                                }

                                // Comprobar llegada a la meta
                                val goalPx = Offset(
                                    x = goalNormalized.x * canvasSize.width,
                                    y = goalNormalized.y * canvasSize.height
                                )
                                val distToGoal = hypot(safePx.x - goalPx.x, safePx.y - goalPx.y)
                                if (distToGoal <= cellMin * 0.42f) {
                                    wins++
                                    status = GameStatus.WON
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            onDragEnd = {
                                if (status == GameStatus.PLAYING) {
                                    status = GameStatus.DROPPED
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            },
                            onDragCancel = {
                                if (status == GameStatus.PLAYING) {
                                    status = GameStatus.DROPPED
                                }
                            }
                        )
                    }
            ) {
                canvasSize = size
                val cellW = size.width / maze.cols
                val cellH = size.height / maze.rows
                val cellMin = min(cellW, cellH)
                val strokeWidth = max(3f, cellMin * 0.08f)

                // Casilla de inicio
                drawRect(
                    color = startColor.copy(alpha = 0.22f),
                    topLeft = Offset.Zero,
                    size = Size(cellW, cellH)
                )

                // Casilla de meta
                drawRect(
                    color = goalColor.copy(alpha = 0.25f),
                    topLeft = Offset((maze.cols - 1) * cellW, (maze.rows - 1) * cellH),
                    size = Size(cellW, cellH)
                )

                // Rastro del recorrido
                if (trailPoints.size >= 2) {
                    for (i in 0 until trailPoints.size - 1) {
                        val p1 = Offset(trailPoints[i].x * size.width, trailPoints[i].y * size.height)
                        val p2 = Offset(trailPoints[i + 1].x * size.width, trailPoints[i + 1].y * size.height)
                        drawLine(
                            color = trailColor,
                            start = p1,
                            end = p2,
                            strokeWidth = cellMin * 0.14f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Muros del laberinto
                for (r in 0 until maze.rows) {
                    for (c in 0 until maze.cols) {
                        val cell = maze.cells[r][c]
                        val left = c * cellW
                        val top = r * cellH
                        val right = left + cellW
                        val bottom = top + cellH

                        if (cell.topWall) {
                            drawLine(
                                color = wallColor,
                                start = Offset(left, top),
                                end = Offset(right, top),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Square
                            )
                        }
                        if (cell.rightWall) {
                            drawLine(
                                color = wallColor,
                                start = Offset(right, top),
                                end = Offset(right, bottom),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Square
                            )
                        }
                        if (cell.bottomWall) {
                            drawLine(
                                color = wallColor,
                                start = Offset(left, bottom),
                                end = Offset(right, bottom),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Square
                            )
                        }
                        if (cell.leftWall) {
                            drawLine(
                                color = wallColor,
                                start = Offset(left, top),
                                end = Offset(left, bottom),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Square
                            )
                        }
                    }
                }

                // Marcador de inicio y meta
                drawCircle(
                    color = startColor,
                    radius = cellMin * 0.18f,
                    center = Offset(startNormalized.x * size.width, startNormalized.y * size.height)
                )
                drawCircle(
                    color = goalColor,
                    radius = cellMin * 0.24f,
                    center = Offset(goalNormalized.x * size.width, goalNormalized.y * size.height)
                )

                // Jugador
                val playerPx = Offset(playerPosNorm.x * size.width, playerPosNorm.y * size.height)
                val playerRadius = cellMin * difficulty.playerRadiusRatio
                drawCircle(
                    color = playerColor,
                    radius = playerRadius,
                    center = playerPx
                )
            }
        }

        // Panel inferior de estado y acciones rápidas
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val statusMessage = when (status) {
                GameStatus.READY -> "Mantén pulsado el punto verde (arriba-izq) y arrastra hasta el rojo"
                GameStatus.PLAYING -> "No toques los muros ni sueltes el dedo"
                GameStatus.CRASHED -> "¡Tocaste un muro! Vuelve al inicio para reintentar"
                GameStatus.DROPPED -> "¡Soltaste el dedo! Toca el inicio para intentarlo de nuevo"
                GameStatus.WON -> "¡Laberinto completado! Genera uno nuevo o reintenta"
                else -> ""
            }

            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (status == GameStatus.CRASHED || status == GameStatus.WON) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },
                color = when (status) {
                    GameStatus.CRASHED, GameStatus.DROPPED -> MaterialTheme.colorScheme.error
                    GameStatus.WON -> Color(0xFF2E7D32)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { resetCurrentMaze(GameStatus.READY) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reiniciar")
                }

                Button(
                    onClick = { generateNewMaze() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (status == GameStatus.WON) "Siguiente laberinto" else "Nuevo laberinto")
                }
            }
        }
    }
}

@Composable
private fun MazeStatPill(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = Modifier.width(104.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun collidesWithMazeWall(
    centerPx: Offset,
    radiusPx: Float,
    wallThicknessPx: Float,
    maze: MazeGrid,
    cellW: Float,
    cellH: Float
): Boolean {
    val halfWall = wallThicknessPx * 0.5f
    val minCol = ((centerPx.x - radiusPx - halfWall) / cellW).toInt().coerceIn(0, maze.cols - 1)
    val maxCol = ((centerPx.x + radiusPx + halfWall) / cellW).toInt().coerceIn(0, maze.cols - 1)
    val minRow = ((centerPx.y - radiusPx - halfWall) / cellH).toInt().coerceIn(0, maze.rows - 1)
    val maxRow = ((centerPx.y + radiusPx + halfWall) / cellH).toInt().coerceIn(0, maze.rows - 1)

    for (r in minRow..maxRow) {
        for (c in minCol..maxCol) {
            val cell = maze.cells[r][c]
            val left = c * cellW
            val top = r * cellH
            val right = left + cellW
            val bottom = top + cellH

            if (cell.topWall && circleIntersectsRect(
                    centerPx,
                    radiusPx,
                    Rect(left - halfWall, top - halfWall, right + halfWall, top + halfWall)
                )
            ) return true

            if (cell.bottomWall && circleIntersectsRect(
                    centerPx,
                    radiusPx,
                    Rect(left - halfWall, bottom - halfWall, right + halfWall, bottom + halfWall)
                )
            ) return true

            if (cell.leftWall && circleIntersectsRect(
                    centerPx,
                    radiusPx,
                    Rect(left - halfWall, top - halfWall, left + halfWall, bottom + halfWall)
                )
            ) return true

            if (cell.rightWall && circleIntersectsRect(
                    centerPx,
                    radiusPx,
                    Rect(right - halfWall, top - halfWall, right + halfWall, bottom + halfWall)
                )
            ) return true
        }
    }
    return false
}

private fun circleIntersectsRect(circleCenter: Offset, radius: Float, rect: Rect): Boolean {
    val closestX = circleCenter.x.coerceIn(rect.left, rect.right)
    val closestY = circleCenter.y.coerceIn(rect.top, rect.bottom)
    val dx = circleCenter.x - closestX
    val dy = circleCenter.y - closestY
    return (dx * dx + dy * dy) < (radius * radius)
}

/**
 * Genera un laberinto aleatorio usando Recursive Backtracker (DFS iterativo)
 * y opcionalmente abre algunos muros extra ("braiding") para suavizar en dificultades bajas.
 */
private fun generateMaze(difficulty: MazeDifficulty): MazeGrid {
    val rows = difficulty.cellsPerSide
    val cols = difficulty.cellsPerSide
    val cells = Array(rows) { r -> Array(cols) { c -> MazeCell(r, c) } }

    val stack = ArrayDeque<MazeCell>()
    val startCell = cells[0][0].apply { visited = true }
    stack.addLast(startCell)

    while (stack.isNotEmpty()) {
        val current = stack.last()
        val unvisitedNeighbors = mutableListOf<Pair<MazeCell, Char>>()

        cellAtOrNull(cells, rows, cols, current.row - 1, current.col)
            ?.takeIf { !it.visited }?.let { unvisitedNeighbors.add(it to 'N') }
        cellAtOrNull(cells, rows, cols, current.row, current.col + 1)
            ?.takeIf { !it.visited }?.let { unvisitedNeighbors.add(it to 'E') }
        cellAtOrNull(cells, rows, cols, current.row + 1, current.col)
            ?.takeIf { !it.visited }?.let { unvisitedNeighbors.add(it to 'S') }
        cellAtOrNull(cells, rows, cols, current.row, current.col - 1)
            ?.takeIf { !it.visited }?.let { unvisitedNeighbors.add(it to 'W') }

        if (unvisitedNeighbors.isNotEmpty()) {
            val (next, dir) = unvisitedNeighbors[Random.nextInt(unvisitedNeighbors.size)]
            removeWallBetween(current, next, dir)
            next.visited = true
            stack.addLast(next)
        } else {
            stack.removeLast()
        }
    }

    if (difficulty.extraBraidsRatio > 0f) {
        val totalCells = rows * cols
        val extraOpenings = (totalCells * difficulty.extraBraidsRatio).toInt()
        repeat(extraOpenings) {
            val r = Random.nextInt(rows)
            val c = Random.nextInt(cols)
            val cell = cells[r][c]
            val candidates = mutableListOf<Pair<MazeCell, Char>>()
            if (r > 0 && cell.topWall) candidates.add(cells[r - 1][c] to 'N')
            if (c < cols - 1 && cell.rightWall) candidates.add(cells[r][c + 1] to 'E')
            if (r < rows - 1 && cell.bottomWall) candidates.add(cells[r + 1][c] to 'S')
            if (c > 0 && cell.leftWall) candidates.add(cells[r][c - 1] to 'W')

            if (candidates.isNotEmpty()) {
                val (neighbor, dir) = candidates[Random.nextInt(candidates.size)]
                removeWallBetween(cell, neighbor, dir)
            }
        }
    }

    return MazeGrid(rows = rows, cols = cols, cells = cells)
}

private fun cellAtOrNull(
    cells: Array<Array<MazeCell>>,
    rows: Int,
    cols: Int,
    r: Int,
    c: Int
): MazeCell? = if (r in 0 until rows && c in 0 until cols) cells[r][c] else null

private fun removeWallBetween(a: MazeCell, b: MazeCell, directionFromA: Char) {
    when (directionFromA) {
        'N' -> {
            a.topWall = false
            b.bottomWall = false
        }
        'E' -> {
            a.rightWall = false
            b.leftWall = false
        }
        'S' -> {
            a.bottomWall = false
            b.topWall = false
        }
        'W' -> {
            a.leftWall = false
            b.rightWall = false
        }
    }
}
