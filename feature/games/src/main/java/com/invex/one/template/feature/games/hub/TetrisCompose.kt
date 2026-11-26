package com.invex.one.template.feature.games.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- CONSTANTES Y MODELOS ---
const val BOARD_WIDTH = 10
const val BOARD_HEIGHT = 20

enum class DifficultyT(val label: String, val baseDelayMs: Long, val scoreMultiplier: Int) {
    EASY("Fácil", 700L, 1),
    MEDIUM("Medio", 450L, 2),
    HARD("Difícil", 220L, 3)
}

enum class TetrominoType(val color: Color, val matrix: List<List<Int>>) {
    I(
        Color(0xFF00BCD4), listOf(
            listOf(0, 0, 0, 0),
            listOf(1, 1, 1, 1),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0)
        )
    ),
    J(
        Color(0xFF3F51B5), listOf(
            listOf(1, 0, 0),
            listOf(1, 1, 1),
            listOf(0, 0, 0)
        )
    ),
    L(
        Color(0xFFFF9800), listOf(
            listOf(0, 0, 1),
            listOf(1, 1, 1),
            listOf(0, 0, 0)
        )
    ),
    O(
        Color(0xFFFFEB3B), listOf(
            listOf(1, 1),
            listOf(1, 1)
        )
    ),
    S(
        Color(0xFF4CAF50), listOf(
            listOf(0, 1, 1),
            listOf(1, 1, 0),
            listOf(0, 0, 0)
        )
    ),
    T(
        Color(0xFF9C27B0), listOf(
            listOf(0, 1, 0),
            listOf(1, 1, 1),
            listOf(0, 0, 0)
        )
    ),
    Z(
        Color(0xFFF44336), listOf(
            listOf(1, 1, 0),
            listOf(0, 1, 1),
            listOf(0, 0, 0)
        )
    );

    companion object {
        fun random(): TetrominoType = entries[Random.nextInt(entries.size)]
    }
}

data class ActivePiece(
    val type: TetrominoType,
    val matrix: List<List<Int>>,
    val x: Int,
    val y: Int
)

// --- LÓGICA DEL JUEGO ---
fun createEmptyBoard(): List<List<Color?>> =
    List(BOARD_HEIGHT) { List(BOARD_WIDTH) { null } }

fun spawnPiece(type: TetrominoType): ActivePiece {
    val startX = (BOARD_WIDTH - type.matrix[0].size) / 2
    return ActivePiece(type = type, matrix = type.matrix, x = startX, y = 0)
}

fun rotateMatrix(matrix: List<List<Int>>): List<List<Int>> {
    val n = matrix.size
    return List(n) { r ->
        List(n) { c ->
            matrix[n - 1 - c][r]
        }
    }
}

fun isValidPosition(
    board: List<List<Color?>>,
    matrix: List<List<Int>>,
    targetX: Int,
    targetY: Int
): Boolean {
    for (r in matrix.indices) {
        for (c in matrix[r].indices) {
            if (matrix[r][c] != 0) {
                val boardX = targetX + c
                val boardY = targetY + r
                if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT) {
                    return false
                }
                if (boardY >= 0 && board[boardY][boardX] != null) {
                    return false
                }
            }
        }
    }
    return true
}

fun mergePiece(board: List<List<Color?>>, piece: ActivePiece): List<List<Color?>> {
    val mutableBoard = board.map { it.toMutableList() }.toMutableList()
    for (r in piece.matrix.indices) {
        for (c in piece.matrix[r].indices) {
            if (piece.matrix[r][c] != 0) {
                val bx = piece.x + c
                val by = piece.y + r
                if (by in 0 until BOARD_HEIGHT && bx in 0 until BOARD_WIDTH) {
                    mutableBoard[by][bx] = piece.type.color
                }
            }
        }
    }
    return mutableBoard
}

fun clearFullLines(board: List<List<Color?>>): Pair<List<List<Color?>>, Int> {
    val remainingRows = board.filter { row -> row.any { it == null } }
    val linesCleared = BOARD_HEIGHT - remainingRows.size
    val newEmptyRows = List(linesCleared) { List<Color?>(BOARD_WIDTH) { null } }
    return (newEmptyRows + remainingRows) to linesCleared
}

// --- UI EN JETPACK COMPOSE ---
@Composable
fun TetrisGameScreen(
    modifier: Modifier
) {
    var board by remember { mutableStateOf(createEmptyBoard()) }
    var currentPiece by remember { mutableStateOf(spawnPiece(TetrominoType.random())) }
    var nextPieceType by remember { mutableStateOf(TetrominoType.random()) }
    var score by remember { mutableIntStateOf(0) }
    var lines by remember { mutableIntStateOf(0) }
    var difficulty by remember { mutableStateOf(DifficultyT.EASY) }
    var isPaused by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    // Nivel dinámico según líneas completadas + dificultad seleccionada
    val level = (lines / 10) + 1
    val tickDelay = remember(difficulty, level) {
        (difficulty.baseDelayMs - (level - 1) * 45L).coerceAtLeast(80L)
    }

    fun resetGame(newDifficulty: DifficultyT = difficulty) {
        difficulty = newDifficulty
        board = createEmptyBoard()
        currentPiece = spawnPiece(TetrominoType.random())
        nextPieceType = TetrominoType.random()
        score = 0
        lines = 0
        isGameOver = false
        isPaused = false
    }

    fun lockAndSpawnNext() {
        val merged = mergePiece(board, currentPiece)
        val (clearedBoard, clearedCount) = clearFullLines(merged)
        board = clearedBoard

        if (clearedCount > 0) {
            lines += clearedCount
            val basePoints = when (clearedCount) {
                1 -> 100
                2 -> 300
                3 -> 500
                else -> 800
            }
            score += basePoints * level * difficulty.scoreMultiplier
        }

        val next = spawnPiece(nextPieceType)
        nextPieceType = TetrominoType.random()
        if (!isValidPosition(board, next.matrix, next.x, next.y)) {
            isGameOver = true
        } else {
            currentPiece = next
        }
    }

    fun moveDown(): Boolean {
        if (isGameOver || isPaused) return false
        return if (isValidPosition(
                board,
                currentPiece.matrix,
                currentPiece.x,
                currentPiece.y + 1
            )
        ) {
            currentPiece = currentPiece.copy(y = currentPiece.y + 1)
            true
        } else {
            lockAndSpawnNext()
            false
        }
    }

    fun moveHorizontal(dx: Int) {
        if (isGameOver || isPaused) return
        val newX = currentPiece.x + dx
        if (isValidPosition(board, currentPiece.matrix, newX, currentPiece.y)) {
            currentPiece = currentPiece.copy(x = newX)
        }
    }

    fun rotatePiece() {
        if (isGameOver || isPaused) return
        val rotated = rotateMatrix(currentPiece.matrix)
        // Wall kick básico (centro, izquierda, derecha)
        val offsets = listOf(0, -1, 1, -2, 2)
        for (offset in offsets) {
            if (isValidPosition(board, rotated, currentPiece.x + offset, currentPiece.y)) {
                currentPiece = currentPiece.copy(matrix = rotated, x = currentPiece.x + offset)
                break
            }
        }
    }

    fun hardDrop() {
        if (isGameOver || isPaused) return
        var dropDistance = 0
        while (isValidPosition(
                board,
                currentPiece.matrix,
                currentPiece.x,
                currentPiece.y + dropDistance + 1
            )
        ) {
            dropDistance++
        }
        score += dropDistance * 2 * difficulty.scoreMultiplier
        currentPiece = currentPiece.copy(y = currentPiece.y + dropDistance)
        lockAndSpawnNext()
    }

    // Bucle principal de caída automática
    LaunchedEffect(isPaused, isGameOver, tickDelay, currentPiece) {
        if (!isPaused && !isGameOver) {
            delay(tickDelay)
            moveDown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Selector de dificultad y Pausa
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DifficultyT.entries.forEach { diff ->
                    FilterChip(
                        selected = difficulty == diff,
                        onClick = { resetGame(diff) },
                        label = { Text(diff.label, fontSize = 12.sp) }
                    )
                }
            }
            OutlinedButton(
                onClick = { isPaused = !isPaused },
                enabled = !isGameOver,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(if (isPaused) "Reanudar" else "Pausa", fontSize = 12.sp)
            }
        }

        // 2. Tablero principal + Panel lateral (Puntaje y Siguiente pieza)
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Canvas del tablero
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(BOARD_WIDTH.toFloat() / BOARD_HEIGHT.toFloat())
                    .border(2.dp, Color.Gray, RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellWidth = size.width / BOARD_WIDTH
                    val cellHeight = size.height / BOARD_HEIGHT
// Cuadrícula de fondo
                    for (r in 0..BOARD_HEIGHT) {
                        drawLine(
                            color = Color(0xFF2C2C2C),
                            start = Offset(0f, r * cellHeight),
                            end = Offset(size.width, r * cellHeight),
                            strokeWidth = 1f
                        )
                    }
                    for (c in 0..BOARD_WIDTH) {
                        drawLine(
                            color = Color(0xFF2C2C2C),
                            start = Offset(c * cellWidth, 0f),
                            end = Offset(c * cellWidth, size.height),
                            strokeWidth = 1f
                        )
                    }
// Celdas fijas en el tablero
                    for (r in 0 until BOARD_HEIGHT) {
                        for (c in 0 until BOARD_WIDTH) {
                            board[r][c]?.let { color ->
                                drawRect(
                                    color = color,
                                    topLeft = Offset(c * cellWidth + 1f, r * cellHeight + 1f),
                                    size = Size(cellWidth - 2f, cellHeight - 2f)
                                )
                            }
                        }
                    }
// Pieza activa cayendo
                    if (!isGameOver) {
                        for (r in currentPiece.matrix.indices) {
                            for (c in currentPiece.matrix[r].indices) {
                                if (currentPiece.matrix[r][c] != 0) {
                                    val drawX = (currentPiece.x + c) * cellWidth
                                    val drawY = (currentPiece.y + r) * cellHeight
                                    if (currentPiece.y + r >= 0) {
                                        drawRect(
                                            color = currentPiece.type.color,
                                            topLeft = Offset(drawX + 1f, drawY + 1f),
                                            size = Size(cellWidth - 2f, cellHeight - 2f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
// Overlay de Pausa o Game Over
                if (isGameOver || isPaused) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.78f)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isGameOver) "GAME OVER" else "PAUSA",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = {
                            if (isGameOver) resetGame() else isPaused = false
                        }) {
                            Text(if (isGameOver) "Reiniciar" else "Continuar")
                        }
                    }
                }
            }
// Panel lateral de estadísticas y siguiente pieza
            Column(
                modifier = Modifier
                    .width(105.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatCard(title = "PUNTOS", value = score.toString())
                StatCard(title = "LÍNEAS", value = lines.toString())
                StatCard(title = "NIVEL", value = level.toString())
// Vista previa de siguiente pieza
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SIGUIENTE", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Canvas(modifier = Modifier.size(64.dp)) {
                        val matrix = nextPieceType.matrix
                        val cellSize = size.width / 4f
                        val offsetX = (4 - matrix[0].size) * cellSize / 2f
                        val offsetY = (4 - matrix.size) * cellSize / 2f
                        for (r in matrix.indices) {
                            for (c in matrix[r].indices) {
                                if (matrix[r][c] != 0) {
                                    drawRect(
                                        color = nextPieceType.color,
                                        topLeft = Offset(
                                            offsetX + c * cellSize + 1f,
                                            offsetY + r * cellSize + 1f
                                        ),
                                        size = Size(cellSize - 2f, cellSize - 2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
// 3. Controles táctiles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(label = "◀", onClick = { moveHorizontal(-1) })
            ControlButton(label = "▼", onClick = { moveDown() })
            ControlButton(label = "▶", onClick = { moveHorizontal(1) })
            ControlButton(label = "↻", onClick = { rotatePiece() })
            ControlButton(label = "⤓", onClick = { hardDrop() })
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 11.sp, color = Color.Gray)
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun ControlButton(label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(58.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = label, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}
