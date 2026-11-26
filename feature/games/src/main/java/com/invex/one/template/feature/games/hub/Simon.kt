package com.invex.one.template.feature.games.hub

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

private enum class SimonPad(
    val label: String,
    val normalColor: Color,
    val activeColor: Color,
    val shape: RoundedCornerShape
) {
    GREEN(
        label = "A",
        normalColor = Color(0xFF1B5E20),
        activeColor = Color(0xFF4CAF50),
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
    ),
    RED(
        label = "B",
        normalColor = Color(0xFF8E0000),
        activeColor = Color(0xFFFF5252),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 36.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
    ),
    YELLOW(
        label = "C",
        normalColor = Color(0xFFF57F17),
        activeColor = Color(0xFFFFEE58),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 36.dp, bottomEnd = 12.dp)
    ),
    BLUE(
        label = "D",
        normalColor = Color(0xFF0D47A1),
        activeColor = Color(0xFF448AFF),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 36.dp)
    )
}

private enum class SimonPhase {
    IDLE,
    SHOWING_SEQUENCE,
    PLAYER_INPUT,
    ROUND_CLEARED,
    GAME_OVER
}

@Composable
fun SimonSaysScreen(
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(SimonPhase.IDLE) }
    var sequence by remember { mutableStateOf<List<SimonPad>>(emptyList()) }
    var playerStepIndex by remember { mutableIntStateOf(0) }
    var highlightedPad by remember { mutableStateOf<SimonPad?>(null) }
    var bestRound by remember { mutableIntStateOf(0) }
    var statusText by remember { mutableStateOf("Presiona Iniciar para memorizar la secuencia") }

    val currentRound = sequence.size
    val haptics = LocalHapticFeedback.current

    fun startNewGame() {
        val firstPad = SimonPad.entries[Random.nextInt(SimonPad.entries.size)]
        sequence = listOf(firstPad)
        playerStepIndex = 0
        highlightedPad = null
        phase = SimonPhase.SHOWING_SEQUENCE
        statusText = "Observa con atención..."
    }

    // Reproduce la secuencia actual paso a paso
    LaunchedEffect(phase, sequence) {
        if (phase == SimonPhase.SHOWING_SEQUENCE && sequence.isNotEmpty()) {
            statusText = "Ronda $currentRound: observa la secuencia"
            delay(450L)

            val speedFactor = (currentRound - 1).coerceAtMost(10)
            val litDurationMs = (520L - speedFactor * 25L).coerceAtLeast(260L)
            val gapDurationMs = (220L - speedFactor * 12L).coerceAtLeast(100L)

            for (pad in sequence) {
                if (phase != SimonPhase.SHOWING_SEQUENCE) return@LaunchedEffect
                highlightedPad = pad
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(litDurationMs)

                highlightedPad = null
                delay(gapDurationMs)
            }

            if (phase == SimonPhase.SHOWING_SEQUENCE) {
                playerStepIndex = 0
                phase = SimonPhase.PLAYER_INPUT
                statusText = "Tu turno: repite ${sequence.size} paso${if (sequence.size == 1) "" else "s"}"
            }
        }
    }

    // Pausa breve al completar una ronda antes de añadir el siguiente paso
    LaunchedEffect(phase) {
        if (phase == SimonPhase.ROUND_CLEARED) {
            statusText = "¡Ronda $currentRound completada!"
            delay(650L)
            val nextPad = SimonPad.entries[Random.nextInt(SimonPad.entries.size)]
            sequence = sequence + nextPad
            playerStepIndex = 0
            phase = SimonPhase.SHOWING_SEQUENCE
        }
    }

    fun onPadPressed(pad: SimonPad) {
        if (phase != SimonPhase.PLAYER_INPUT || sequence.isEmpty()) return

        val expectedPad = sequence[playerStepIndex]
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)

        if (pad == expectedPad) {
            val nextIndex = playerStepIndex + 1
            if (nextIndex >= sequence.size) {
                if (currentRound > bestRound) {
                    bestRound = currentRound
                }
                phase = SimonPhase.ROUND_CLEARED
            } else {
                playerStepIndex = nextIndex
                statusText = "Bien (${playerStepIndex}/${sequence.size})"
            }
        } else {
            if (currentRound > bestRound) {
                bestRound = currentRound
            }
            highlightedPad = null
            phase = SimonPhase.GAME_OVER
            statusText = "¡Fallaste en la ronda $currentRound!"
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
        // Cabecera de rondas y progreso
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SimonStatCard(
                    label = "RONDA",
                    value = if (currentRound > 0) currentRound.toString() else "-"
                )
                SimonStatCard(
                    label = "PROGRESO",
                    value = if (phase == SimonPhase.PLAYER_INPUT) {
                        "${playerStepIndex}/${sequence.size}"
                    } else {
                        "0/${sequence.size}"
                    }
                )
                SimonStatCard(
                    label = "RÉCORD",
                    value = if (bestRound > 0) bestRound.toString() else "-"
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when (phase) {
                        SimonPhase.GAME_OVER -> MaterialTheme.colorScheme.error
                        SimonPhase.ROUND_CLEARED -> Color(0xFF2E7D32)
                        SimonPhase.PLAYER_INPUT -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }

        // Tablero 2x2 de Simón Dice
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(44.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SimonPadButton(
                        pad = SimonPad.GREEN,
                        isLit = highlightedPad == SimonPad.GREEN,
                        enabled = phase == SimonPhase.PLAYER_INPUT,
                        onPress = { onPadPressed(SimonPad.GREEN) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    SimonPadButton(
                        pad = SimonPad.RED,
                        isLit = highlightedPad == SimonPad.RED,
                        enabled = phase == SimonPhase.PLAYER_INPUT,
                        onPress = { onPadPressed(SimonPad.RED) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SimonPadButton(
                        pad = SimonPad.YELLOW,
                        isLit = highlightedPad == SimonPad.YELLOW,
                        enabled = phase == SimonPhase.PLAYER_INPUT,
                        onPress = { onPadPressed(SimonPad.YELLOW) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    SimonPadButton(
                        pad = SimonPad.BLUE,
                        isLit = highlightedPad == SimonPad.BLUE,
                        enabled = phase == SimonPhase.PLAYER_INPUT,
                        onPress = { onPadPressed(SimonPad.BLUE) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            // Botón central flotante al iniciar o reiniciar tras perder
            if (phase == SimonPhase.IDLE || phase == SimonPhase.GAME_OVER) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    modifier = Modifier.padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = if (phase == SimonPhase.GAME_OVER) "¡Juego terminado!" else "Simón Dice",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (phase == SimonPhase.GAME_OVER) {
                            Text(
                                text = "Llegaste a la ronda $currentRound",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { startNewGame() },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (phase == SimonPhase.GAME_OVER) "Reintentar" else "Iniciar",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        // Pie informativo
        Text(
            text = "Repite el orden exacto desde el primer paso hasta el último",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SimonStatCard(
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

@Composable
private fun SimonPadButton(
    pad: SimonPad,
    isLit: Boolean,
    enabled: Boolean,
    onPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (isLit) pad.activeColor else pad.normalColor,
        animationSpec = tween(durationMillis = 110),
        label = "padColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isLit) 1.04f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "padScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(pad.shape)
            .background(color)
            .border(
                width = if (isLit) 3.dp else 1.5.dp,
                color = if (isLit) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.25f),
                shape = pad.shape
            )
            .clickable(
                enabled = enabled,
                onClick = onPress
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = pad.label,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White.copy(alpha = if (isLit) 1f else 0.75f)
        )
    }
}
