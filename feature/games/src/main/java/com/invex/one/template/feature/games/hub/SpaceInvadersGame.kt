package com.invex.one.template.feature.games.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.random.Random

private enum class InvaderDifficulty(
    val label: String,
    val rows: Int,
    val cols: Int,
    val baseMarchSpeed: Float, // unidades normalizadas/seg
    val enemyFireIntervalSec: Float,
    val enemyBulletSpeed: Float,
    val startLives: Int
) {
    EASY(
        label = "Fácil",
        rows = 3,
        cols = 6,
        baseMarchSpeed = 0.14f,
        enemyFireIntervalSec = 1.35f,
        enemyBulletSpeed = 0.42f,
        startLives = 4
    ),
    NORMAL(
        label = "Normal",
        rows = 4,
        cols = 7,
        baseMarchSpeed = 0.21f,
        enemyFireIntervalSec = 0.95f,
        enemyBulletSpeed = 0.56f,
        startLives = 3
    ),
    HARD(
        label = "Difícil",
        rows = 5,
        cols = 7,
        baseMarchSpeed = 0.30f,
        enemyFireIntervalSec = 0.65f,
        enemyBulletSpeed = 0.72f,
        startLives = 2
    )
}

private data class Invader(
    val id: Int,
    val row: Int,
    val col: Int,
    val x: Float, // centro X normalizado (0f..1f)
    val y: Float, // centro Y normalizado (0f..1f)
    val alive: Boolean = true
)

private data class Bullet(
    val id: Int,
    val x: Float,
    val y: Float,
    val vy: Float,
    val fromPlayer: Boolean
)

@Composable
fun SpaceInvadersScreen(
    modifier: Modifier = Modifier
) {
    var difficulty by remember { mutableStateOf(InvaderDifficulty.NORMAL) }
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var wonAllWaves by remember { mutableStateOf(false) }

    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(difficulty.startLives) }
    var wave by remember { mutableIntStateOf(1) }

    // Nave del jugador (coordenadas normalizadas 0f..1f)
    var playerX by remember { mutableFloatStateOf(0.5f) }
    val playerY = 0.90f
    val playerWidth = 0.14f
    val playerHeight = 0.045f

    // Formación de invasores
    var invaders by remember { mutableStateOf<List<Invader>>(emptyList()) }
    var marchDir by remember { mutableFloatStateOf(1f) } // 1f = derecha, -1f = izquierda
    var marchSpeed by remember { mutableFloatStateOf(difficulty.baseMarchSpeed) }

    // Proyectiles y temporizadores
    var bullets by remember { mutableStateOf<List<Bullet>>(emptyList()) }
    var nextId by remember { mutableIntStateOf(1) }
    var playerFireCooldown by remember { mutableFloatStateOf(0f) }
    var enemyFireTimer by remember { mutableFloatStateOf(0f) }

    val haptics = LocalHapticFeedback.current

    fun createFormation(diff: InvaderDifficulty, waveNumber: Int): List<Invader> {
        val list = mutableListOf<Invader>()
        val leftMargin = 0.14f
        val rightMargin = 0.86f
        val spanX = rightMargin - leftMargin
        val stepX = if (diff.cols > 1) spanX / (diff.cols - 1) else 0f

        val topStart = 0.12f + ((waveNumber - 1) * 0.025f).coerceAtMost(0.08f)
        val stepY = 0.075f

        for (r in 0 until diff.rows) {
            for (c in 0 until diff.cols) {
                list.add(
                    Invader(
                        id = nextId++,
                        row = r,
                        col = c,
                        x = leftMargin + c * stepX,
                        y = topStart + r * stepY,
                        alive = true
                    )
                )
            }
        }
        return list
    }

    fun startNewGame() {
        score = 0
        wave = 1
        lives = difficulty.startLives
        playerX = 0.5f
        marchDir = 1f
        marchSpeed = difficulty.baseMarchSpeed
        bullets = emptyList()
        playerFireCooldown = 0f
        enemyFireTimer = 0f
        invaders = createFormation(difficulty, waveNumber = 1)
        isGameOver = false
        wonAllWaves = false
        isPlaying = true
    }

    fun firePlayerBullet() {
        if (!isPlaying || playerFireCooldown > 0f) return
        val activePlayerBullets = bullets.count { it.fromPlayer }
        if (activePlayerBullets >= 3) return // Limita ráfaga para mantener el reto táctico

        bullets = bullets + Bullet(
            id = nextId++,
            x = playerX,
            y = playerY - playerHeight * 0.65f,
            vy = -0.88f,
            fromPlayer = true
        )
        playerFireCooldown = 0.24f
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    LaunchedEffect(difficulty) {
        if (!isPlaying) {
            lives = difficulty.startLives
            marchSpeed = difficulty.baseMarchSpeed
            invaders = createFormation(difficulty, waveNumber = 1)
        }
    }

    // Bucle principal por frame
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

                if (playerFireCooldown > 0f) {
                    playerFireCooldown = (playerFireCooldown - dt).coerceAtLeast(0f)
                }

                // 1. Mover invasores y comprobar borde
                val aliveInvaders = invaders.filter { it.alive }
                val invaderHalfW = 0.042f
                val invaderHalfH = 0.022f

                var nextDir = marchDir
                var shouldDrop = false
                val dxStep = marchDir * marchSpeed * dt

                for (inv in aliveInvaders) {
                    val candidateX = inv.x + dxStep
                    if (candidateX + invaderHalfW >= 0.97f && marchDir > 0f) {
                        nextDir = -1f
                        shouldDrop = true
                        break
                    } else if (candidateX - invaderHalfW <= 0.03f && marchDir < 0f) {
                        nextDir = 1f
                        shouldDrop = true
                        break
                    }
                }

                val dropAmount = if (shouldDrop) 0.035f else 0f
                marchDir = nextDir

                var movedInvaders = invaders.map { inv ->
                    if (!inv.alive) inv
                    else inv.copy(
                        x = inv.x + (if (shouldDrop) 0f else dxStep),
                        y = inv.y + dropAmount
                    )
                }

                // Si algún invasor llega a la altura de la nave, fin de partida
                if (movedInvaders.any { it.alive && it.y + invaderHalfH >= playerY - playerHeight * 0.5f }) {
                    invaders = movedInvaders
                    isPlaying = false
                    isGameOver = true
                    if (score > bestScore) bestScore = score
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    return@withFrameNanos
                }

                // 2. Disparo enemigo periódico desde columnas más bajas vivas
                enemyFireTimer += dt
                val currentFireInterval = (difficulty.enemyFireIntervalSec - (wave - 1) * 0.08f)
                    .coerceAtLeast(0.35f)

                var updatedBullets = bullets.mapNotNull { b ->
                    val ny = b.y + b.vy * dt
                    if (ny < -0.04f || ny > 1.04f) null else b.copy(y = ny)
                }.toMutableList()

                if (enemyFireTimer >= currentFireInterval) {
                    enemyFireTimer = 0f
                    val lowestByCol = movedInvaders
                        .filter { it.alive }
                        .groupBy { it.col }
                        .values
                        .mapNotNull { colList -> colList.maxByOrNull { it.row } }

                    if (lowestByCol.isNotEmpty()) {
                        val shooter = lowestByCol[Random.nextInt(lowestByCol.size)]
                        val speedScale = 1f + ((wave - 1) * 0.10f).coerceAtMost(0.5f)
                        updatedBullets.add(
                            Bullet(
                                id = nextId++,
                                x = shooter.x,
                                y = shooter.y + invaderHalfH + 0.015f,
                                vy = difficulty.enemyBulletSpeed * speedScale,
                                fromPlayer = false
                            )
                        )
                    }
                }

                // 3. Colisiones de proyectiles
                val consumedBulletIds = mutableSetOf<Int>()
                val hitInvaderIds = mutableSetOf<Int>()
                var playerHitThisFrame = false

                for (b in updatedBullets) {
                    if (b.id in consumedBulletIds) continue

                    if (b.fromPlayer) {
                        val hitInv = movedInvaders.firstOrNull { inv ->
                            inv.alive &&
                                    inv.id !in hitInvaderIds &&
                                    abs(b.x - inv.x) <= invaderHalfW + 0.014f &&
                                    abs(b.y - inv.y) <= invaderHalfH + 0.016f
                        }
                        if (hitInv != null) {
                            consumedBulletIds.add(b.id)
                            hitInvaderIds.add(hitInv.id)
                            val rowBonus = (difficulty.rows - hitInv.row) * 10
                            score += (20 + rowBonus) * wave
                            if (score > bestScore) bestScore = score
                            // Acelera ligeramente la formación conforme quedan menos invasores
                            marchSpeed = (marchSpeed + 0.004f).coerceAtMost(difficulty.baseMarchSpeed * 2.4f)
                        }
                    } else {
                        val hitPlayer = abs(b.x - playerX) <= playerWidth * 0.48f &&
                                abs(b.y - playerY) <= playerHeight * 0.55f
                        if (hitPlayer) {
                            consumedBulletIds.add(b.id)
                            playerHitThisFrame = true
                        }
                    }
                }

                if (hitInvaderIds.isNotEmpty()) {
                    movedInvaders = movedInvaders.map { inv ->
                        if (inv.id in hitInvaderIds) inv.copy(alive = false) else inv
                    }
                }

                updatedBullets = updatedBullets.filterNot { it.id in consumedBulletIds }.toMutableList()

                if (playerHitThisFrame) {
                    lives -= 1
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (lives <= 0) {
                        invaders = movedInvaders
                        bullets = updatedBullets
                        isPlaying = false
                        isGameOver = true
                        if (score > bestScore) bestScore = score
                        return@withFrameNanos
                    }
                }

                // 4. Comprobar oleada completada
                if (movedInvaders.none { it.alive }) {
                    if (wave >= 3) {
                        invaders = movedInvaders
                        bullets = emptyList()
                        isPlaying = false
                        wonAllWaves = true
                        if (score > bestScore) bestScore = score
                        return@withFrameNanos
                    } else {
                        wave += 1
                        marchDir = 1f
                        marchSpeed = difficulty.baseMarchSpeed * (1f + (wave - 1) * 0.18f)
                        bullets = emptyList()
                        movedInvaders = createFormation(difficulty, wave)
                    }
                }

                invaders = movedInvaders
                bullets = updatedBullets
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
        // Cabecera
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InvaderStatCard(label = "PUNTOS", value = score.toString())
                InvaderStatCard(label = "OLEADA", value = "$wave/3")
                InvaderStatCard(
                    label = "VIDAS",
                    value = "♥".repeat(lives.coerceAtLeast(0)).ifEmpty { "0" }
                )
            }

            if (!isPlaying) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InvaderDifficulty.entries.forEach { option ->
                            val selected = option == difficulty
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary
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
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Campo de batalla
        val spaceBg = Color(0xFF090D16)
        val playerColor = Color(0xFF38BDF8)
        val playerBulletColor = Color(0xFFFACC15)
        val enemyBulletColor = Color(0xFFFB7185)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.82f)
                .clip(RoundedCornerShape(20.dp))
                .background(spaceBg)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp)
                )
                .pointerInput(isPlaying) {
                    detectTapGestures(
                        onTap = { firePlayerBullet() }
                    )
                }
                .pointerInput(isPlaying) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        if (!isPlaying || canvasWidthPx <= 0f) return@detectHorizontalDragGestures
                        val halfW = playerWidth * 0.5f
                        playerX = (playerX + (dragAmount / canvasWidthPx)).coerceIn(halfW + 0.02f, 0.98f - halfW)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                canvasWidthPx = size.width

                // Invasores
                val invW = 0.072f * size.width
                val invH = 0.036f * size.height
                invaders.forEach { inv ->
                    if (!inv.alive) return@forEach
                    val invColor = when (inv.row % 3) {
                        0 -> Color(0xFFC084FC)
                        1 -> Color(0xFF34D399)
                        else -> Color(0xFFFBBF24)
                    }
                    val left = inv.x * size.width - invW * 0.5f
                    val top = inv.y * size.height - invH * 0.5f

                    drawRoundRect(
                        color = invColor,
                        topLeft = Offset(left, top),
                        size = Size(invW, invH),
                        cornerRadius = CornerRadius(invH * 0.35f, invH * 0.35f)
                    )
                }

                // Proyectiles
                bullets.forEach { b ->
                    val bx = b.x * size.width
                    val by = b.y * size.height
                    if (b.fromPlayer) {
                        drawRoundRect(
                            color = playerBulletColor,
                            topLeft = Offset(bx - 3.5f, by - 12f),
                            size = Size(7f, 24f),
                            cornerRadius = CornerRadius(3.5f, 3.5f)
                        )
                    } else {
                        drawCircle(
                            color = enemyBulletColor,
                            radius = 6f,
                            center = Offset(bx, by)
                        )
                    }
                }

                // Nave del jugador (triángulo/nave estilizada)
                val px = playerX * size.width
                val py = playerY * size.height
                val pw = playerWidth * size.width
                val ph = playerHeight * size.height

                val shipPath = Path().apply {
                    moveTo(px, py - ph * 0.55f)
                    lineTo(px + pw * 0.5f, py + ph * 0.45f)
                    lineTo(px, py + ph * 0.22f)
                    lineTo(px - pw * 0.5f, py + ph * 0.45f)
                    close()
                }
                drawPath(path = shipPath, color = playerColor)
            }

            if (!isPlaying) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = when {
                                wonAllWaves -> "¡Sector despejado!"
                                isGameOver -> "¡Nave destruida!"
                                else -> "Space Invaders"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (wonAllWaves || isGameOver) {
                            Text(
                                text = "Puntos: $score  •  Récord: $bestScore",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { startNewGame() },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (wonAllWaves || isGameOver) "Jugar de nuevo" else "Iniciar")
                        }
                    }
                }
            }
        }

        // Controles táctiles de apoyo + instrucción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Arrastra para mover • Toca para disparar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { firePlayerBullet() },
                enabled = isPlaying,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text("Disparar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InvaderStatCard(
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
