package com.invex.one.template.feature.games.hub

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val GRID_SIZE = 9 // 3x3
private const val GAME_DURATION_SECONDS = 30

private enum class Difficulty(
    val label: String,
    val baseVisibleMs: Long,
    val minVisibleMs: Long,
    val basePauseMs: Long,
    val bombChance: Float,
    val bombPenalty: Int,
    val speedStepMs: Long
) {
    EASY(
        label = "Fácil",
        baseVisibleMs = 950L,
        minVisibleMs = 520L,
        basePauseMs = 420L,
        bombChance = 0.15f,
        bombPenalty = 2,
        speedStepMs = 60L
    ),
    NORMAL(
        label = "Normal",
        baseVisibleMs = 800L,
        minVisibleMs = 380L,
        basePauseMs = 320L,
        bombChance = 0.25f,
        bombPenalty = 4,
        speedStepMs = 85L
    ),
    HARD(
        label = "Difícil",
        baseVisibleMs = 640L,
        minVisibleMs = 270L,
        basePauseMs = 220L,
        bombChance = 0.35f,
        bombPenalty = 6,
        speedStepMs = 110L
    )
}

private enum class TargetType {
    MOLE,
    BOMB
}

private data class ActiveTarget(
    val holeIndex: Int,
    val type: TargetType,
    val wasHit: Boolean = false
)

@Composable
fun WhackAMoleScreen(
    modifier: Modifier = Modifier
) {
    // Configuración y estado general
    var difficulty by remember { mutableStateOf(Difficulty.NORMAL) }
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    // Marcadores
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var maxCombo by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(GAME_DURATION_SECONDS) }

    // Objetivo visible actualmente
    var activeTarget by remember { mutableStateOf<ActiveTarget?>(null) }

    // Feedback visual rápido al golpear (+3, -4, ¡COMBO!)
    var floatingFeedback by remember { mutableStateOf<String?>(null) }
    var floatingFeedbackIsPositive by remember { mutableStateOf(true) }

    val haptics = LocalHapticFeedback.current
    val multiplier = (1 + (combo / 3)).coerceAtMost(5)

    // Limpia el mensaje flotante tras un breve instante
    LaunchedEffect(floatingFeedback) {
        if (floatingFeedback != null) {
            delay(650L)
            floatingFeedback = null
        }
    }

    // Bucle 1: Temporizador de la partida
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            isPlaying = false
            isGameOver = true
            activeTarget = null
            if (score > highScore) {
                highScore = score
            }
        }
    }

    // Bucle 2: Aparición y ocultamiento de topos y bombas
    LaunchedEffect(isPlaying, difficulty) {
        if (isPlaying) {
            while (isPlaying && timeLeft > 0) {
                val progressionStep = (score / 8).coerceAtMost(6)
                val visibleDuration =
                    (difficulty.baseVisibleMs - (progressionStep * difficulty.speedStepMs))
                        .coerceAtLeast(difficulty.minVisibleMs)
                val pauseDuration =
                    (difficulty.basePauseMs - (progressionStep * (difficulty.speedStepMs / 2)))
                        .coerceAtLeast(120L)

                delay(pauseDuration)
                if (!isPlaying) break

                val previousHole = activeTarget?.holeIndex ?: -1
                var nextHole: Int
                do {
                    nextHole = Random.nextInt(GRID_SIZE)
                } while (nextHole == previousHole)

                val nextType = if (Random.nextFloat() < difficulty.bombChance) {
                    TargetType.BOMB
                } else {
                    TargetType.MOLE
                }

                activeTarget = ActiveTarget(
                    holeIndex = nextHole,
                    type = nextType,
                    wasHit = false
                )

                delay(visibleDuration)

                // Oculta el objetivo si aún no ha sido golpeado
                if (activeTarget?.holeIndex == nextHole && activeTarget?.wasHit == false) {
                    activeTarget = null
                }
            }
        }
    }

    fun startGame() {
        score = 0
        combo = 0
        maxCombo = 0
        timeLeft = GAME_DURATION_SECONDS
        activeTarget = null
        floatingFeedback = null
        isGameOver = false
        isPlaying = true
    }

    fun onHoleTapped(index: Int) {
        if (!isPlaying) return
        val current = activeTarget

        if (current != null && current.holeIndex == index && !current.wasHit) {
            activeTarget = current.copy(wasHit = true)

            when (current.type) {
                TargetType.MOLE -> {
                    combo++
                    if (combo > maxCombo) maxCombo = combo
                    val currentMultiplier = (1 + (combo / 3)).coerceAtMost(5)
                    val pointsEarned = 1 * currentMultiplier
                    score += pointsEarned
                    activeTarget = null

                    floatingFeedbackIsPositive = true
                    floatingFeedback = if (currentMultiplier > 1) {
                        "+$pointsEarned (x$currentMultiplier)"
                    } else {
                        "+$pointsEarned"
                    }
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                TargetType.BOMB -> {
                    combo = 0
                    score = (score - difficulty.bombPenalty).coerceAtLeast(0)
                    activeTarget = null

                    floatingFeedbackIsPositive = false
                    floatingFeedback = "-${difficulty.bombPenalty} ¡Bomba!"
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        } else {
            // Golpear un agujero vacío rompe el combo y penaliza levemente
            if (combo > 0) {
                combo = 0
                floatingFeedbackIsPositive = false
                floatingFeedback = "Combo perdido"
            }
            if (score > 0) {
                score--
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabecera: Marcadores y estado de combo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ScoreHeader(
                score = score,
                highScore = highScore,
                timeLeft = timeLeft
            )

            ComboAndFeedbackBar(
                combo = combo,
                multiplier = multiplier,
                feedback = floatingFeedback,
                isPositive = floatingFeedbackIsPositive,
                difficultyLabel = difficulty.label
            )
        }

        // Tablero 3x3
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF8D6E63).copy(alpha = 0.25f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            val targetType = activeTarget
                                ?.takeIf { it.holeIndex == index && !it.wasHit }
                                ?.type

                            MoleHole(
                                targetType = targetType,
                                onTap = { onHoleTapped(index) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            }

            // Superposición de inicio o fin de partida
            if (!isPlaying) {
                GameOverlay(
                    isGameOver = isGameOver,
                    finalScore = score,
                    maxCombo = maxCombo,
                    selectedDifficulty = difficulty,
                    onSelectDifficulty = { difficulty = it },
                    onStartClick = { startGame() }
                )
            }
        }

        // Pie con regla rápida
        Text(
            text = if (isPlaying) {
                "Toca 🐹 y evita 💣 • Cada 3 aciertos sube el multiplicador"
            } else {
                "Elige dificultad y supera tu récord en ${GAME_DURATION_SECONDS}s"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ScoreHeader(
    score: Int,
    highScore: Int,
    timeLeft: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCard(label = "PUNTOS", value = score.toString())
        StatCard(
            label = "TIEMPO",
            value = "${timeLeft}s",
            highlight = timeLeft in 1..5
        )
        StatCard(label = "RÉCORD", value = highScore.toString())
    }
}

@Composable
private fun ComboAndFeedbackBar(
    combo: Int,
    multiplier: Int,
    feedback: String?,
    isPositive: Boolean,
    difficultyLabel: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (combo > 0) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ) {
            Text(
                text = "Racha: $combo  •  x$multiplier",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (combo > 0) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        if (feedback != null) {
            Text(
                text = feedback,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = "Modo: $difficultyLabel",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    val textColor by animateColorAsState(
        targetValue = if (highlight) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        label = "timerColor"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun MoleHole(
    targetType: TargetType?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = targetType != null
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "targetScale"
    )

    // Mantiene el último tipo visible mientras termina de encogerse
    var lastRenderedType by remember { mutableStateOf<TargetType?>(null) }
    if (targetType != null) {
        lastRenderedType = targetType
    }

    val displayedType = targetType ?: lastRenderedType
    val targetBgColor = when (displayedType) {
        TargetType.MOLE -> Color(0xFFFFB74D)
        TargetType.BOMB -> Color(0xFFEF5350)
        null -> Color(0xFFFFB74D)
    }
    val emoji = when (displayedType) {
        TargetType.MOLE -> "🐹"
        TargetType.BOMB -> "💣"
        null -> "🐹"
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFF4E342E))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.82f)
                .clip(CircleShape)
                .background(Color(0xFF3E2723))
        )

        if (scale > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.75f)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(targetBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 36.sp
                )
            }
        }
    }
}

@Composable
private fun GameOverlay(
    isGameOver: Boolean,
    finalScore: Int,
    maxCombo: Int,
    selectedDifficulty: Difficulty,
    onSelectDifficulty: (Difficulty) -> Unit,
    onStartClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isGameOver) "¡Tiempo terminado!" else "Whack-a-Mole",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (isGameOver) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Puntos: $finalScore  •  Mejor racha: $maxCombo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Dificultad",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Difficulty.entries.forEach { option ->
                    val selected = option == selectedDifficulty
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
                            .clickable { onSelectDifficulty(option) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
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

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onStartClick,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Text(
                    text = if (isGameOver) "Jugar de nuevo" else "Iniciar partida",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
