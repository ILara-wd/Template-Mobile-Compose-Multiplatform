package com.invex.one.template.feature.games.hub

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private const val BOARD_SIZE = 4

private enum class SwipeDirection {
    UP, DOWN, LEFT, RIGHT
}

private data class TileCell(
    val id: Int,
    val value: Int,
    val isMergedThisTurn: Boolean = false,
    val isNewThisTurn: Boolean = false
)

private data class BoardSnapshot(
    val board: List<List<TileCell?>>,
    val score: Int
)

@Composable
fun Game2048Screen(
    modifier: Modifier = Modifier
) {
    var nextTileId by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    var board by remember {
        mutableStateOf<List<List<TileCell?>>>(emptyList())
    }
    var previousSnapshot by remember { mutableStateOf<BoardSnapshot?>(null) }
    var hasReached2048 by remember { mutableStateOf(false) }
    var continueAfter2048 by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    val haptics = LocalHapticFeedback.current

    fun createInitialBoard(): List<List<TileCell?>> {
        val grid = Array(BOARD_SIZE) { arrayOfNulls<TileCell>(BOARD_SIZE) }
        val positions = (0 until BOARD_SIZE * BOARD_SIZE).shuffled()
        positions.take(2).forEach { pos ->
            val r = pos / BOARD_SIZE
            val c = pos % BOARD_SIZE
            val value = if (Random.nextFloat() < 0.9f) 2 else 4
            grid[r][c] = TileCell(
                id = nextTileId++,
                value = value,
                isNewThisTurn = true
            )
        }
        return grid.map { it.toList() }
    }

    fun startNewGame() {
        score = 0
        previousSnapshot = null
        hasReached2048 = false
        continueAfter2048 = false
        isGameOver = false
        board = createInitialBoard()
    }

    LaunchedEffect(Unit) {
        if (board.isEmpty()) {
            board = createInitialBoard()
        }
    }

    fun applyMove(direction: SwipeDirection) {
        if (board.isEmpty() || isGameOver) return
        if (hasReached2048 && !continueAfter2048) return

        val currentGrid = Array(BOARD_SIZE) { r ->
            Array(BOARD_SIZE) { c ->
                board[r][c]?.copy(isMergedThisTurn = false, isNewThisTurn = false)
            }
        }

        var moved = false
        var gainedScore = 0
        var hit2048ThisMove = false

        fun processLine(line: List<TileCell?>): List<TileCell?> {
            val nonEmpty = line.filterNotNull().toMutableList()
            val merged = mutableListOf<TileCell?>()

            var i = 0
            while (i < nonEmpty.size) {
                val current = nonEmpty[i]
                if (i + 1 < nonEmpty.size && nonEmpty[i + 1].value == current.value) {
                    val partner = nonEmpty[i + 1]
                    val combinedValue = current.value * 2
                    gainedScore += combinedValue
                    if (combinedValue >= 2048) {
                        hit2048ThisMove = true
                    }
                    merged.add(
                        TileCell(
                            id = current.id,
                            value = combinedValue,
                            isMergedThisTurn = true,
                            isNewThisTurn = false
                        )
                    )
                    i += 2
                } else {
                    merged.add(current)
                    i += 1
                }
            }

            while (merged.size < BOARD_SIZE) {
                merged.add(null)
            }
            return merged
        }

        val resultGrid = Array(BOARD_SIZE) { arrayOfNulls<TileCell>(BOARD_SIZE) }

        when (direction) {
            SwipeDirection.LEFT -> {
                for (r in 0 until BOARD_SIZE) {
                    val original = (0 until BOARD_SIZE).map { c -> currentGrid[r][c] }
                    val updated = processLine(original)
                    for (c in 0 until BOARD_SIZE) {
                        resultGrid[r][c] = updated[c]
                        if (resultGrid[r][c]?.id != original[c]?.id || resultGrid[r][c]?.value != original[c]?.value) {
                            moved = true
                        }
                    }
                }
            }

            SwipeDirection.RIGHT -> {
                for (r in 0 until BOARD_SIZE) {
                    val originalReversed = (BOARD_SIZE - 1 downTo 0).map { c -> currentGrid[r][c] }
                    val updatedReversed = processLine(originalReversed)
                    for (idx in 0 until BOARD_SIZE) {
                        val c = BOARD_SIZE - 1 - idx
                        val origCell = currentGrid[r][c]
                        val newCell = updatedReversed[idx]
                        resultGrid[r][c] = newCell
                        if (newCell?.id != origCell?.id || newCell?.value != origCell?.value) {
                            moved = true
                        }
                    }
                }
            }

            SwipeDirection.UP -> {
                for (c in 0 until BOARD_SIZE) {
                    val original = (0 until BOARD_SIZE).map { r -> currentGrid[r][c] }
                    val updated = processLine(original)
                    for (r in 0 until BOARD_SIZE) {
                        resultGrid[r][c] = updated[r]
                        if (resultGrid[r][c]?.id != original[r]?.id || resultGrid[r][c]?.value != original[r]?.value) {
                            moved = true
                        }
                    }
                }
            }

            SwipeDirection.DOWN -> {
                for (c in 0 until BOARD_SIZE) {
                    val originalReversed = (BOARD_SIZE - 1 downTo 0).map { r -> currentGrid[r][c] }
                    val updatedReversed = processLine(originalReversed)
                    for (idx in 0 until BOARD_SIZE) {
                        val r = BOARD_SIZE - 1 - idx
                        val origCell = currentGrid[r][c]
                        val newCell = updatedReversed[idx]
                        resultGrid[r][c] = newCell
                        if (newCell?.id != origCell?.id || newCell?.value != origCell?.value) {
                            moved = true
                        }
                    }
                }
            }
        }

        if (!moved) return

        // Guardar estado previo para permitir Deshacer
        previousSnapshot = BoardSnapshot(
            board = board.map { row -> row.map { it?.copy(isMergedThisTurn = false, isNewThisTurn = false) } },
            score = score
        )

        // Generar nueva ficha en celda vacía aleatoria
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                if (resultGrid[r][c] == null) {
                    emptyCells.add(r to c)
                }
            }
        }

        if (emptyCells.isNotEmpty()) {
            val (spawnR, spawnC) = emptyCells[Random.nextInt(emptyCells.size)]
            val spawnValue = if (Random.nextFloat() < 0.9f) 2 else 4
            resultGrid[spawnR][spawnC] = TileCell(
                id = nextTileId++,
                value = spawnValue,
                isNewThisTurn = true
            )
        }

        val nextBoard = resultGrid.map { it.toList() }
        board = nextBoard
        score += gainedScore
        if (score > bestScore) {
            bestScore = score
        }

        if (hit2048ThisMove) {
            hasReached2048 = true
        }

        if (!hasAvailableMoves(nextBoard)) {
            isGameOver = true
        }

        haptics.performHapticFeedback(
            if (gainedScore > 0) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
        )
    }

    fun undoMove() {
        val snap = previousSnapshot ?: return
        board = snap.board
        score = snap.score
        previousSnapshot = null
        isGameOver = false
    }

    var dragAccumulated by remember { mutableStateOf(Offset.Zero) }
    var swipeHandledInGesture by remember { mutableStateOf(false) }
    val swipeThresholdPx = 48f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8EF))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabecera: Título, marcadores y botones de acción
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2048",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF776E65)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScoreBox2048(label = "PUNTOS", value = score)
                    ScoreBox2048(label = "RÉCORD", value = bestScore)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Desliza para unir fichas iguales",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF776E65)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { undoMove() },
                        enabled = previousSnapshot != null,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Deshacer", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = { startNewGame() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8F7A66),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Reiniciar", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Tablero 4x4 con detección de deslizado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFBBADA0))
                .pointerInput(board, isGameOver, hasReached2048, continueAfter2048) {
                    detectDragGestures(
                        onDragStart = {
                            dragAccumulated = Offset.Zero
                            swipeHandledInGesture = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (swipeHandledInGesture) return@detectDragGestures

                            dragAccumulated += dragAmount
                            val dx = dragAccumulated.x
                            val dy = dragAccumulated.y

                            if (abs(dx) > swipeThresholdPx || abs(dy) > swipeThresholdPx) {
                                swipeHandledInGesture = true
                                val dir = if (abs(dx) > abs(dy)) {
                                    if (dx > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                                } else {
                                    if (dy > 0) SwipeDirection.DOWN else SwipeDirection.UP
                                }
                                applyMove(dir)
                            }
                        },
                        onDragEnd = {
                            dragAccumulated = Offset.Zero
                            swipeHandledInGesture = false
                        },
                        onDragCancel = {
                            dragAccumulated = Offset.Zero
                            swipeHandledInGesture = false
                        }
                    )
                }
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            if (board.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (r in 0 until BOARD_SIZE) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (c in 0 until BOARD_SIZE) {
                                val cell = board[r][c]
                                TileSlot(
                                    cell = cell,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                        }
                    }
                }
            }

            // Overlay de victoria (2048) o derrota (sin movimientos)
            if (hasReached2048 && !continueAfter2048 && !isGameOver) {
                EndStateOverlay2048(
                    title = "¡Llegaste a 2048!",
                    subtitle = "Puntuación actual: $score",
                    primaryButtonText = "Seguir jugando",
                    onPrimaryClick = { continueAfter2048 = true },
                    secondaryButtonText = "Nueva partida",
                    onSecondaryClick = { startNewGame() }
                )
            } else if (isGameOver) {
                EndStateOverlay2048(
                    title = "Sin más movimientos",
                    subtitle = "Puntuación final: $score",
                    primaryButtonText = "Jugar de nuevo",
                    onPrimaryClick = { startNewGame() },
                    secondaryButtonText = null,
                    onSecondaryClick = null
                )
            }
        }

        // Pie informativo
        Text(
            text = "Combina fichas del mismo número hasta llegar a 2048",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF776E65)
        )
    }
}

@Composable
private fun ScoreBox2048(
    label: String,
    value: Int
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFBBADA0))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEEE4DA)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
private fun TileSlot(
    cell: TileCell?,
    modifier: Modifier = Modifier
) {
    val targetScale = when {
        cell == null -> 1f
        cell.isNewThisTurn -> 0.88f
        cell.isMergedThisTurn -> 1.12f
        else -> 1f
    }

    // Pequeño pulso al aparecer o fusionarse
    var pulseTrigger by remember(cell?.id, cell?.value, cell?.isMergedThisTurn, cell?.isNewThisTurn) {
        mutableStateOf(cell != null && (cell.isNewThisTurn || cell.isMergedThisTurn))
    }

    LaunchedEffect(pulseTrigger) {
        if (pulseTrigger) {
            delay(40L)
            pulseTrigger = false
        }
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (pulseTrigger) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tileScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (cell == null) Color(0xFFCDC1B4) else tileBackgroundColor(cell.value),
        animationSpec = tween(durationMillis = 120),
        label = "tileBg"
    )

    val textColor = if (cell == null || cell.value <= 4) Color(0xFF776E65) else Color(0xFFF9F6F2)
    val fontSize = when {
        cell == null -> 24.sp
        cell.value < 100 -> 28.sp
        cell.value < 1000 -> 24.sp
        else -> 20.sp
    }

    Box(
        modifier = modifier
            .scale(if (cell != null) animatedScale else 1f)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (cell != null) {
            Text(
                text = cell.value.toString(),
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}

@Composable
private fun EndStateOverlay2048(
    title: String,
    subtitle: String,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String?,
    onSecondaryClick: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFAF8EF).copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF776E65)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF776E65)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onPrimaryClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8F7A66),
                    contentColor = Color.White
                )
            ) {
                Text(primaryButtonText, fontWeight = FontWeight.Bold)
            }
            if (secondaryButtonText != null && onSecondaryClick != null) {
                TextButton(onClick = onSecondaryClick) {
                    Text(
                        text = secondaryButtonText,
                        color = Color(0xFF776E65),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun tileBackgroundColor(value: Int): Color = when (value) {
    2 -> Color(0xFFEEE4DA)
    4 -> Color(0xFFEDE0C8)
    8 -> Color(0xFFF2B179)
    16 -> Color(0xFFF59563)
    32 -> Color(0xFFF67C5F)
    64 -> Color(0xFFF65E3B)
    128 -> Color(0xFFEDCF72)
    256 -> Color(0xFFEDCC61)
    512 -> Color(0xFFEDC850)
    1024 -> Color(0xFFEDC53F)
    2048 -> Color(0xFFEDC22E)
    else -> Color(0xFF3C3A32)
}

private fun hasAvailableMoves(board: List<List<TileCell?>>): Boolean {
    for (r in 0 until BOARD_SIZE) {
        for (c in 0 until BOARD_SIZE) {
            val current = board[r][c] ?: return true
            if (c + 1 < BOARD_SIZE && board[r][c + 1]?.value == current.value) return true
            if (r + 1 < BOARD_SIZE && board[r + 1][c]?.value == current.value) return true
        }
    }
    return false
}
