package com.invex.one.template.feature.games.hub

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.invex.one.template.core.designsystem.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.invex.one.template.feature.games.hub.character.GameSprites
import com.invex.one.template.feature.games.hub.character.drawCenteredSprite
import com.invex.one.template.feature.games.hub.character.rememberGameSprites
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Modelos y configuración de dificultad
// ---------------------------------------------------------------------------

enum class DodgeGamePhase {
    DIFFICULTY_SELECT,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class DodgeDifficulty(
    val label: String,
    val baseSpawnInterval: Float,
    val spawnDecayPerLevel: Float,
    val baseSpeedMultiplier: Float,
    val speedStepPerLevel: Float,
    val maxActiveBase: Int,
    val pointsPerLevel: Int,
    val scoreMultiplier: Float
) {
    EASY(
        label = "Fácil",
        baseSpawnInterval = 1.15f,
        spawnDecayPerLevel = 0.06f,
        baseSpeedMultiplier = 0.85f,
        speedStepPerLevel = 0.12f,
        maxActiveBase = 6,
        pointsPerLevel = 500,
        scoreMultiplier = 0.8f
    ),
    NORMAL(
        label = "Normal",
        baseSpawnInterval = 0.90f,
        spawnDecayPerLevel = 0.07f,
        baseSpeedMultiplier = 1.05f,
        speedStepPerLevel = 0.15f,
        maxActiveBase = 8,
        pointsPerLevel = 500,
        scoreMultiplier = 1.0f
    ),
    HARD(
        label = "Difícil",
        baseSpawnInterval = 0.68f,
        spawnDecayPerLevel = 0.08f,
        baseSpeedMultiplier = 1.35f,
        speedStepPerLevel = 0.18f,
        maxActiveBase = 11,
        pointsPerLevel = 650,
        scoreMultiplier = 1.35f
    )
}

enum class DodgeSoundEvent {
    COLLECT,
    SHIELD_UP,
    DASH,
    HIT,
    GAME_OVER
}

enum class HazardKind {
    FALLING, // Círculo que cae y rebota lateralmente
    BARRIER, // Rectángulo ancho que exige moverse o hacer dash
    WAVE     // Peligro que oscila horizontalmente al caer
}

enum class CollectibleKind {
    ORB,   // Suma puntos y sube combo
    SHIELD // Otorga escudo temporal contra 1 golpe
}

data class DodgeHazard(
    val id: Long,
    val kind: HazardKind,
    var x: Float,
    var y: Float,
    val baseX: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val width: Float,
    val height: Float,
    var elapsedTime: Float = 0f,
    val waveAmplitude: Float = 0f,
    val waveFrequency: Float = 0f
)

data class DodgeCollectible(
    val id: Long,
    val kind: CollectibleKind,
    var x: Float,
    var y: Float,
    val baseX: Float,
    var vy: Float,
    val radius: Float,
    var elapsedTime: Float = 0f,
    val waveAmplitude: Float = 0f,
    val waveFrequency: Float = 0f
)

data class DodgeParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val color: Color,
    var age: Float = 0f,
    val maxAge: Float = 0.45f
) {
    val progress: Float get() = (age / maxAge).coerceIn(0f, 1f)
    val isExpired: Boolean get() = age >= maxAge
}

data class DodgePopup(
    val text: String,
    var x: Float,
    var y: Float,
    val vy: Float,
    val color: Color,
    val fontSizeSp: Float = 19f,
    var age: Float = 0f,
    val maxAge: Float = 0.75f
) {
    val progress: Float get() = (age / maxAge).coerceIn(0f, 1f)
    val isExpired: Boolean get() = age >= maxAge
}

data class DodgeLevelConfig(
    val level: Int,
    val spawnIntervalSeconds: Float,
    val speedMultiplier: Float,
    val maxActiveEntities: Int
)

fun configForDodgeLevel(difficulty: DodgeDifficulty, level: Int): DodgeLevelConfig {
    val clampedLevel = max(1, level)
    val levelOffset = clampedLevel - 1
    return DodgeLevelConfig(
        level = clampedLevel,
        spawnIntervalSeconds = max(
            0.22f,
            difficulty.baseSpawnInterval - levelOffset * difficulty.spawnDecayPerLevel
        ),
        speedMultiplier = difficulty.baseSpeedMultiplier + levelOffset * difficulty.speedStepPerLevel,
        maxActiveEntities = (difficulty.maxActiveBase + levelOffset * 2).coerceAtMost(32)
    )
}

// ---------------------------------------------------------------------------
// Reproductor de sonido con SoundPool (baja latencia en Android)
// ---------------------------------------------------------------------------

class DodgeSoundPoolPlayer(
    context: Context,
    private val soundResMap: Map<DodgeSoundEvent, Int>
) {
    private val soundPool: SoundPool
    private val loadedIds = mutableMapOf<DodgeSoundEvent, Int>()
    private val readySamples = mutableSetOf<Int>()

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                readySamples.add(sampleId)
            }
        }

        soundResMap.forEach { (event, resId) ->
            loadedIds[event] = soundPool.load(context, resId, 1)
        }
    }

    fun play(event: DodgeSoundEvent, pitchRate: Float = 1.0f) {
        val sampleId = loadedIds[event] ?: return
        if (sampleId !in readySamples) return

        val clampedRate = pitchRate.coerceIn(0.5f, 2.0f)
        soundPool.play(
            sampleId,
            /* leftVolume = */ 1.0f,
            /* rightVolume = */ 1.0f,
            /* priority = */ 1,
            /* loop = */ 0,
            /* rate = */ clampedRate
        )
    }

    fun release() {
        soundPool.release()
    }
}

@Composable
fun rememberDodgeSoundPlayer(
    soundResMap: Map<DodgeSoundEvent, Int>
): DodgeSoundPoolPlayer {
    val context = LocalContext.current.applicationContext
    val player = remember(soundResMap) {
        DodgeSoundPoolPlayer(context, soundResMap)
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    return player
}

// ---------------------------------------------------------------------------
// Controlador principal de Dodge & Dash
// ---------------------------------------------------------------------------

class DodgeGameController(
    val initialLives: Int = 3,
    initialDifficulty: DodgeDifficulty = DodgeDifficulty.NORMAL,
    startOnDifficultySelect: Boolean = true,
    private val onSoundEvent: ((DodgeSoundEvent, Float) -> Unit)? = null
) {
    var difficulty by mutableStateOf(initialDifficulty)
        private set
    var phase by mutableStateOf(
        if (startOnDifficultySelect) DodgeGamePhase.DIFFICULTY_SELECT else DodgeGamePhase.PLAYING
    )
        private set

    var score by mutableIntStateOf(0)
        private set
    var bestScore by mutableIntStateOf(0)
        private set
    var combo by mutableIntStateOf(0)
        private set
    var level by mutableIntStateOf(1)
        private set
    var lives by mutableIntStateOf(initialLives)
        private set
    var hasShield by mutableStateOf(false)
        private set

    // Progreso de cooldown del Dash: 1.0f = listo, 0.0f..1.0f = recargando
    var dashReadyRatio by mutableFloatStateOf(1f)
        private set

    var frameTick by mutableLongStateOf(0L)
        private set

    private var boardWidth = 0f
    private var boardHeight = 0f

    // Estado del jugador
    private val playerRadius = 32f
    private var playerX = 0f
    private var playerY = 0f
    private var invulnerabilityTimer = 0f
    private var dashActiveTimer = 0f
    private var dashCooldownTimer = 0f
    private val dashDuration = 0.16f
    private val dashCooldownDuration = 1.25f
    private var lastHorizontalDirection = 1f

    private val hazards = mutableListOf<DodgeHazard>()
    private val collectibles = mutableListOf<DodgeCollectible>()
    private val particles = mutableListOf<DodgeParticle>()
    private val popups = mutableListOf<DodgePopup>()

    private var nextId = 1L
    private var spawnAccumulator = 0f
    private var survivalScoreAccumulator = 0f

    fun updateBoardSize(width: Float, height: Float) {
        val wasUninitialized = boardWidth <= 0f || boardHeight <= 0f
        boardWidth = width
        boardHeight = height
        playerY = height - 150f.coerceAtMost(height * 0.18f)

        if (wasUninitialized && width > 0f) {
            playerX = width / 2f
        } else {
            playerX = playerX.coerceIn(
                playerRadius + 8f,
                max(playerRadius + 8f, width - playerRadius - 8f)
            )
        }
    }

    fun selectDifficulty(selected: DodgeDifficulty) {
        difficulty = selected
    }

    fun startGame(selectedDifficulty: DodgeDifficulty = difficulty) {
        difficulty = selectedDifficulty
        hazards.clear()
        collectibles.clear()
        particles.clear()
        popups.clear()

        score = 0
        combo = 0
        level = 1
        lives = initialLives
        hasShield = false
        invulnerabilityTimer = 0f
        dashActiveTimer = 0f
        dashCooldownTimer = 0f
        dashReadyRatio = 1f
        spawnAccumulator = 0f
        survivalScoreAccumulator = 0f

        if (boardWidth > 0f) {
            playerX = boardWidth / 2f
        }
        phase = DodgeGamePhase.PLAYING
        frameTick++
    }

    fun openDifficultySelector() {
        phase = DodgeGamePhase.DIFFICULTY_SELECT
        frameTick++
    }

    fun pauseGame() {
        if (phase == DodgeGamePhase.PLAYING) {
            phase = DodgeGamePhase.PAUSED
            frameTick++
        }
    }

    fun resumeGame() {
        if (phase == DodgeGamePhase.PAUSED) {
            phase = DodgeGamePhase.PLAYING
            frameTick++
        }
    }

    fun togglePause() {
        when (phase) {
            DodgeGamePhase.PLAYING -> pauseGame()
            DodgeGamePhase.PAUSED -> resumeGame()
            else -> Unit
        }
    }

    fun dragPlayerBy(deltaX: Float) {
        if (phase != DodgeGamePhase.PLAYING || boardWidth <= 0f) return
        if (abs(deltaX) > 0.5f) {
            lastHorizontalDirection = if (deltaX > 0f) 1f else -1f
        }
        val minX = playerRadius + 8f
        val maxX = max(minX, boardWidth - playerRadius - 8f)
        playerX = (playerX + deltaX).coerceIn(minX, maxX)
        frameTick++
    }

    fun triggerDash(tapOffset: Offset? = null): Boolean {
        if (phase != DodgeGamePhase.PLAYING || boardWidth <= 0f) return false
        if (dashCooldownTimer > 0f || dashActiveTimer > 0f) return false

        val direction = if (tapOffset != null && abs(tapOffset.x - playerX) > 12f) {
            if (tapOffset.x > playerX) 1f else -1f
        } else {
            lastHorizontalDirection
        }
        lastHorizontalDirection = direction

        val dashDistance = 155f * direction
        val minX = playerRadius + 8f
        val maxX = max(minX, boardWidth - playerRadius - 8f)
        playerX = (playerX + dashDistance).coerceIn(minX, maxX)

        dashActiveTimer = dashDuration
        dashCooldownTimer = dashCooldownDuration
        dashReadyRatio = 0f

        onSoundEvent?.invoke(DodgeSoundEvent.DASH, 1.1f)
        spawnBurst(
            x = playerX,
            y = playerY,
            color = Color(0xFF4DD0E1),
            count = 10,
            maxSpeed = 240f
        )
        frameTick++
        return true
    }

    fun step(deltaSeconds: Float) {
        if (boardWidth <= 0f || boardHeight <= 0f) return
        if (phase == DodgeGamePhase.DIFFICULTY_SELECT || phase == DodgeGamePhase.PAUSED) {
            return
        }

        updateParticlesAndPopups(deltaSeconds)

        if (phase != DodgeGamePhase.PLAYING) {
            frameTick++
            return
        }

        // Timers del jugador
        if (invulnerabilityTimer > 0f) {
            invulnerabilityTimer = max(0f, invulnerabilityTimer - deltaSeconds)
        }
        if (dashActiveTimer > 0f) {
            dashActiveTimer = max(0f, dashActiveTimer - deltaSeconds)
        }
        if (dashCooldownTimer > 0f) {
            dashCooldownTimer = max(0f, dashCooldownTimer - deltaSeconds)
            dashReadyRatio = 1f - (dashCooldownTimer / dashCooldownDuration).coerceIn(0f, 1f)
        } else {
            dashReadyRatio = 1f
        }

        // Puntos pasivos por supervivencia
        survivalScoreAccumulator += deltaSeconds
        if (survivalScoreAccumulator >= 0.5f) {
            survivalScoreAccumulator -= 0.5f
            val passivePoints = (15 * difficulty.scoreMultiplier).toInt().coerceAtLeast(5)
            addScore(passivePoints)
        }

        level = 1 + (score / difficulty.pointsPerLevel)
        val config = configForDodgeLevel(difficulty, level)

        spawnAccumulator += deltaSeconds
        val totalEntities = hazards.size + collectibles.size
        if (spawnAccumulator >= config.spawnIntervalSeconds && totalEntities < config.maxActiveEntities) {
            spawnAccumulator = 0f
            spawnWaveElement(config.speedMultiplier)
        }

        updateHazards(deltaSeconds)
        if (phase != DodgeGamePhase.PLAYING) {
            frameTick++
            return
        }

        updateCollectibles(deltaSeconds)
        frameTick++
    }

    private fun addScore(points: Int) {
        score += points
        if (score > bestScore) {
            bestScore = score
        }
    }

    private fun spawnWaveElement(speedMultiplier: Float) {
        val roll = Random.nextFloat()
        if (roll < 0.72f) {
            spawnHazard(speedMultiplier)
        } else {
            spawnCollectible(speedMultiplier)
        }
    }

    private fun spawnHazard(speedMultiplier: Float) {
        val kindRoll = Random.nextFloat()
        val kind = when {
            kindRoll < 0.50f -> HazardKind.FALLING
            kindRoll < 0.78f -> HazardKind.WAVE
            else -> HazardKind.BARRIER
        }

        val vy = when (kind) {
            HazardKind.FALLING -> Random.nextFloat() * 110f + 220f
            HazardKind.WAVE -> Random.nextFloat() * 90f + 190f
            HazardKind.BARRIER -> Random.nextFloat() * 80f + 175f
        } * speedMultiplier

        when (kind) {
            HazardKind.BARRIER -> {
                val width = min(boardWidth * 0.36f, Random.nextFloat() * 80f + 140f)
                val height = 34f
                val safeSpan = (boardWidth - width - 24f).coerceAtLeast(1f)
                val x = Random.nextFloat() * safeSpan + 12f
                hazards.add(
                    DodgeHazard(
                        id = nextId++,
                        kind = kind,
                        x = x,
                        y = -height,
                        baseX = x,
                        vx = 0f,
                        vy = vy,
                        radius = 0f,
                        width = width,
                        height = height
                    )
                )
            }

            HazardKind.WAVE -> {
                val radius = 26f
                val amplitude = Random.nextFloat() * 45f + 35f
                val safeSpan = (boardWidth - (radius + amplitude) * 2).coerceAtLeast(1f)
                val centerX = Random.nextFloat() * safeSpan + radius + amplitude
                hazards.add(
                    DodgeHazard(
                        id = nextId++,
                        kind = kind,
                        x = centerX,
                        y = -radius,
                        baseX = centerX,
                        vx = 0f,
                        vy = vy,
                        radius = radius,
                        width = radius * 2,
                        height = radius * 2,
                        waveAmplitude = amplitude,
                        waveFrequency = Random.nextFloat() * 3f + 4f
                    )
                )
            }

            HazardKind.FALLING -> {
                val radius = Random.nextFloat() * 10f + 24f
                val safeSpan = (boardWidth - radius * 2).coerceAtLeast(1f)
                val x = Random.nextFloat() * safeSpan + radius
                val dir = if (Random.nextBoolean()) 1f else -1f
                val vx =
                    (Random.nextFloat() * 110f + 60f) * dir * speedMultiplier.coerceAtMost(1.8f)
                hazards.add(
                    DodgeHazard(
                        id = nextId++,
                        kind = kind,
                        x = x,
                        y = -radius,
                        baseX = x,
                        vx = vx,
                        vy = vy,
                        radius = radius,
                        width = radius * 2,
                        height = radius * 2
                    )
                )
            }
        }
    }

    private fun spawnCollectible(speedMultiplier: Float) {
        val kind = if (Random.nextFloat() < 0.84f) CollectibleKind.ORB else CollectibleKind.SHIELD
        val radius = if (kind == CollectibleKind.ORB) 20f else 24f
        val safeSpan = (boardWidth - radius * 2).coerceAtLeast(1f)
        val x = Random.nextFloat() * safeSpan + radius
        val vy = (Random.nextFloat() * 75f + 165f) * speedMultiplier

        collectibles.add(
            DodgeCollectible(
                id = nextId++,
                kind = kind,
                x = x,
                y = -radius,
                baseX = x,
                vy = vy,
                radius = radius,
                waveAmplitude = if (kind == CollectibleKind.ORB) 26f else 0f,
                waveFrequency = if (kind == CollectibleKind.ORB) 3.5f else 0f
            )
        )
    }

    private fun updateHazards(deltaSeconds: Float) {
        val it = hazards.iterator()
        while (it.hasNext()) {
            val h = it.next()
            h.elapsedTime += deltaSeconds
            h.y += h.vy * deltaSeconds

            when (h.kind) {
                HazardKind.FALLING -> {
                    h.x += h.vx * deltaSeconds
                    if (h.x - h.radius < 0f) {
                        h.x = h.radius
                        h.vx = abs(h.vx)
                    } else if (h.x + h.radius > boardWidth) {
                        h.x = boardWidth - h.radius
                        h.vx = -abs(h.vx)
                    }
                }

                HazardKind.WAVE -> {
                    val offset = sin(h.elapsedTime * h.waveFrequency) * h.waveAmplitude
                    h.x = (h.baseX + offset).coerceIn(
                        h.radius,
                        max(h.radius, boardWidth - h.radius)
                    )
                }

                HazardKind.BARRIER -> Unit
            }

            if (checkPlayerHazardCollision(h)) {
                it.remove()
                onPlayerHitByHazard(h)
                if (phase != DodgeGamePhase.PLAYING) break
                continue
            }

            val bottomEdge = if (h.kind == HazardKind.BARRIER) h.y + h.height else h.y - h.radius
            if (bottomEdge > boardHeight) {
                it.remove()
            }
        }
    }

    private fun checkPlayerHazardCollision(hazard: DodgeHazard): Boolean {
        // Invulnerable durante el Dash o tras recibir daño
        if (dashActiveTimer > 0f || invulnerabilityTimer > 0f) return false

        return when (hazard.kind) {
            HazardKind.BARRIER -> {
                val closestX = playerX.coerceIn(hazard.x, hazard.x + hazard.width)
                val closestY = playerY.coerceIn(hazard.y, hazard.y + hazard.height)
                hypot(playerX - closestX, playerY - closestY) < playerRadius * 0.88f
            }

            HazardKind.FALLING, HazardKind.WAVE -> {
                val dist = hypot(playerX - hazard.x, playerY - hazard.y)
                dist < (playerRadius + hazard.radius) * 0.85f
            }
        }
    }

    private fun onPlayerHitByHazard(hazard: DodgeHazard) {
        val impactX = if (hazard.kind == HazardKind.BARRIER) playerX else hazard.x
        val impactY = if (hazard.kind == HazardKind.BARRIER) playerY else hazard.y

        if (hasShield) {
            hasShield = false
            invulnerabilityTimer = 0.65f
            onSoundEvent?.invoke(DodgeSoundEvent.COLLECT, 0.75f)
            spawnBurst(
                x = impactX,
                y = impactY,
                color = Color(0xFF64B5F6),
                count = 14,
                maxSpeed = 280f
            )
            popups.add(
                DodgePopup(
                    text = "ESCUDO!",
                    x = playerX,
                    y = playerY - 32f,
                    vy = -95f,
                    color = Color(0xFF64B5F6)
                )
            )
            return
        }

        combo = 0
        lives = max(0, lives - 1)
        invulnerabilityTimer = 1.0f

        spawnBurst(x = impactX, y = impactY, color = Color(0xFFEF5350), count = 16, maxSpeed = 320f)
        popups.add(
            DodgePopup(
                text = "-1 VIDA",
                x = playerX,
                y = playerY - 32f,
                vy = -100f,
                color = Color(0xFFEF5350)
            )
        )

        if (lives == 0) {
            phase = DodgeGamePhase.GAME_OVER
            if (score > bestScore) bestScore = score
            onSoundEvent?.invoke(DodgeSoundEvent.GAME_OVER, 1.0f)
        } else {
            onSoundEvent?.invoke(DodgeSoundEvent.HIT, 0.9f)
        }
    }

    private fun updateCollectibles(deltaSeconds: Float) {
        val it = collectibles.iterator()
        while (it.hasNext()) {
            val item = it.next()
            item.elapsedTime += deltaSeconds
            item.y += item.vy * deltaSeconds

            if (item.waveAmplitude > 0f) {
                val offset = sin(item.elapsedTime * item.waveFrequency) * item.waveAmplitude
                item.x = (item.baseX + offset).coerceIn(
                    item.radius,
                    max(item.radius, boardWidth - item.radius)
                )
            }

            val dist = hypot(playerX - item.x, playerY - item.y)
            if (dist <= playerRadius + item.radius * 1.15f) {
                onCollectItem(item)
                it.remove()
                continue
            }

            if (item.y - item.radius > boardHeight) {
                if (item.kind == CollectibleKind.ORB) {
                    combo = 0
                }
                it.remove()
            }
        }
    }

    private fun onCollectItem(item: DodgeCollectible) {
        when (item.kind) {
            CollectibleKind.ORB -> {
                combo += 1
                val multiplier = 1 + (combo / 5)
                val earned =
                    (100 * multiplier * difficulty.scoreMultiplier).toInt().coerceAtLeast(20)
                addScore(earned)

                val pitch = (1.0f + (combo % 10) * 0.04f).coerceAtMost(1.4f)
                onSoundEvent?.invoke(DodgeSoundEvent.COLLECT, pitch)
                spawnBurst(
                    x = item.x,
                    y = item.y,
                    color = Color(0xFFFFD54F),
                    count = 12,
                    maxSpeed = 250f
                )
                popups.add(
                    DodgePopup(
                        text = "+$earned",
                        x = item.x,
                        y = item.y - 12f,
                        vy = -115f,
                        color = Color(0xFFFFF59D)
                    )
                )
            }

            CollectibleKind.SHIELD -> {
                hasShield = true
                val bonus = (75 * difficulty.scoreMultiplier).toInt().coerceAtLeast(25)
                addScore(bonus)
                onSoundEvent?.invoke(DodgeSoundEvent.SHIELD_UP, 1.1f)
                spawnBurst(
                    x = item.x,
                    y = item.y,
                    color = Color(0xFF42A5F5),
                    count = 14,
                    maxSpeed = 260f
                )
                popups.add(
                    DodgePopup(
                        text = "ESCUDO +$bonus",
                        x = item.x,
                        y = item.y - 12f,
                        vy = -110f,
                        color = Color(0xFF64B5F6)
                    )
                )
            }
        }
    }

    private fun updateParticlesAndPopups(deltaSeconds: Float) {
        val pIt = particles.iterator()
        while (pIt.hasNext()) {
            val p = pIt.next()
            p.age += deltaSeconds
            if (p.isExpired) {
                pIt.remove()
                continue
            }
            p.x += p.vx * deltaSeconds
            p.y += p.vy * deltaSeconds
            p.vx *= (1f - 3.5f * deltaSeconds).coerceAtLeast(0f)
            p.vy *= (1f - 3.5f * deltaSeconds).coerceAtLeast(0f)
        }

        val popIt = popups.iterator()
        while (popIt.hasNext()) {
            val pop = popIt.next()
            pop.age += deltaSeconds
            if (pop.isExpired) {
                popIt.remove()
                continue
            }
            pop.y += pop.vy * deltaSeconds
        }
    }

    private fun spawnBurst(x: Float, y: Float, color: Color, count: Int, maxSpeed: Float) {
        if (particles.size >= 260) return
        val allowed = minOf(count, 260 - particles.size)
        for (i in 0 until allowed) {
            val angle = (2.0 * PI * i / allowed) + (Random.nextFloat() * 0.45 - 0.225)
            val speed = Random.nextFloat() * maxSpeed * 0.6f + maxSpeed * 0.4f
            particles.add(
                DodgeParticle(
                    x = x,
                    y = y,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    radius = Random.nextFloat() * 6f + 4f,
                    color = color,
                    maxAge = Random.nextFloat() * 0.2f + 0.35f
                )
            )
        }
    }

    fun renderScene(drawScope: DrawScope, textMeasurer: TextMeasurer, sprites: GameSprites?) {
        val unused = frameTick
        with(drawScope) {
            // 1. Peligros
            for (h in hazards) {
                when (h.kind) {
                    HazardKind.BARRIER -> {
                        drawRoundRect(
                            color = Color(0xFFE53935),
                            topLeft = Offset(h.x, h.y),
                            size = Size(h.width, h.height),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }

                    HazardKind.WAVE -> {
                        drawCircle(
                            color = Color(0xFFFF7043),
                            radius = h.radius,
                            center = Offset(h.x, h.y)
                        )
                    }

                    HazardKind.FALLING -> {
                        drawCircle(
                            color = Color(0xFFEF5350),
                            radius = h.radius,
                            center = Offset(h.x, h.y)
                        )
                    }
                }
            }

            // 2. Recolectables
            for ((_, kind, x, y, _, _, radius) in collectibles) {
                //val color = when (item.kind) {
                //    CollectibleKind.ORB -> Color(0xFFFFD54F)
                //    CollectibleKind.SHIELD -> Color(0xFF42A5F5)
                //}
                //drawCircle(
                //    color = color,
                //    radius = item.radius,
                //    center = Offset(item.x, item.y)
                //)
                // Ejemplo al dibujar un recolectable dentro de renderScene:
                drawCenteredSprite(
                    sprite = if (kind == CollectibleKind.ORB) sprites?.orb else sprites?.shield,
                    centerX = x,
                    centerY = y,
                    diameter = radius * 2f
                ) {
                    val color = when (kind) {
                        CollectibleKind.ORB -> Color(0xFFFFD54F)
                        CollectibleKind.SHIELD -> Color(0xFF42A5F5)
                    }
                    drawCircle(
                        color = color,
                        radius = radius,
                        center = Offset(x, y)
                    )
                }
            }

            // 3. Jugador (con parpadeo en i-frames y halo en Dash/Escudo)
            if (boardWidth > 0f && boardHeight > 0f) {
                val blinkHide =
                    invulnerabilityTimer > 0f && (((invulnerabilityTimer * 16f).toInt() % 2) == 0)
                if (!blinkHide) {
                    if (dashActiveTimer > 0f) {
                        drawCircle(
                            color = Color(0xFF4DD0E1).copy(alpha = 0.35f),
                            radius = playerRadius * 1.45f,
                            center = Offset(playerX, playerY)
                        )
                    }

                    drawCircle(
                        color = Color(0xFF26C6DA),
                        radius = playerRadius,
                        center = Offset(playerX, playerY)
                    )

                    if (hasShield) {
                        drawCircle(
                            color = Color(0xFF64B5F6),
                            radius = playerRadius + 8f,
                            center = Offset(playerX, playerY),
                            style = Stroke(width = 4f)
                        )
                    }
                }
            }

            // 4. Partículas
            for (p in particles) {
                drawCircle(
                    color = p.color,
                    radius = p.radius * (1f - 0.5f * p.progress),
                    center = Offset(p.x, p.y),
                    alpha = 1f - p.progress
                )
            }

            // 5. Textos flotantes
            for (popup in popups) {
                val alpha = 1f - popup.progress
                val layout = textMeasurer.measure(
                    text = popup.text,
                    style = TextStyle(
                        color = popup.color.copy(alpha = alpha),
                        fontSize = popup.fontSizeSp.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                val maxX = (size.width - layout.size.width - 16f).coerceAtLeast(16f)
                val topLeft = Offset(
                    x = (popup.x - layout.size.width / 2f).coerceIn(16f, maxX),
                    y = (popup.y - layout.size.height / 2f).coerceAtLeast(16f)
                )
                drawText(textLayoutResult = layout, topLeft = topLeft)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Pantalla principal, HUD, controles de Dash y overlays
// ---------------------------------------------------------------------------

@Composable
fun DodgeGameScreen(
    modifier: Modifier = Modifier,
    soundResMap: Map<DodgeSoundEvent, Int> = emptyMap(),
    initialLives: Int = 3,
    initialDifficulty: DodgeDifficulty = DodgeDifficulty.NORMAL,
) {
    val haptics = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()
    val lifecycleOwner = LocalLifecycleOwner.current
    val soundPlayer = rememberDodgeSoundPlayer(soundResMap = soundResMap)

// 1. Cargar sprites una sola vez a nivel de pantalla
    val sprites = rememberGameSprites(
        playerResId = R.drawable.img_1, // Ej. R.drawable.ic_player_ship
        orbResId = R.drawable.img_3,    // Ej. R.drawable.ic_energy_orb
        shieldResId = R.drawable.img_4  // Ej. R.drawable.ic_shield_core
    )

    val controller = remember(initialLives, initialDifficulty) {
        DodgeGameController(
            initialLives = initialLives,
            initialDifficulty = initialDifficulty,
            startOnDifficultySelect = true,
            onSoundEvent = { event, pitch ->
                soundPlayer.play(event = event, pitchRate = pitch)
            }
        )
    }

    // Botón Back en Android para pausar o reanudar
    //val handleBack =
    //    controller.phase == DodgeGamePhase.PLAYING || controller.phase == DodgeGamePhase.PAUSED
    //BackHandler(enabled = handleBack) {
    //    controller.togglePause()
    //}



    // Pausa automática al pasar a segundo plano
    DisposableEffect(lifecycleOwner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                controller.pauseGame()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(controller) {
        var lastFrameTimeNanos = 0L
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos != 0L) {
                    val rawDelta = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                    controller.step(rawDelta.coerceAtMost(0.05f))
                }
                lastFrameTimeNanos = frameTimeNanos
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF101418))
    ) {
        // Tablero con arrastre para mover y tap para Dash
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(controller) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        controller.dragPlayerBy(dragAmount.x)
                    }
                }
                .pointerInput(controller) {
                    detectTapGestures { offset ->
                        if (controller.triggerDash(offset)) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                }
        ) {
            controller.updateBoardSize(size.width, size.height)
            //controller.renderScene(this, textMeasurer)
            // 2. Pasar `sprites` al renderizador del Canvas
            controller.renderScene(
                drawScope = this,
                textMeasurer = textMeasurer,
                sprites = sprites
            )
        }

        if (controller.phase != DodgeGamePhase.DIFFICULTY_SELECT) {
            DodgeTopHud(
                score = controller.score,
                combo = controller.combo,
                level = controller.level,
                difficultyLabel = controller.difficulty.label,
                lives = controller.lives,
                maxLives = controller.initialLives,
                hasShield = controller.hasShield,
                isPaused = controller.phase == DodgeGamePhase.PAUSED,
                onTogglePause = { controller.togglePause() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(12.dp)
            )

            // Indicador/botón inferior de Dash
            DodgeBottomBar(
                dashReadyRatio = controller.dashReadyRatio,
                onDashClick = {
                    if (controller.triggerDash()) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            )
        }

        when (controller.phase) {
            DodgeGamePhase.DIFFICULTY_SELECT -> {
                DodgeDifficultyOverlay(
                    selectedDifficulty = controller.difficulty,
                    bestScore = controller.bestScore,
                    onSelectDifficulty = { controller.selectDifficulty(it) },
                    onStartGame = { controller.startGame(controller.difficulty) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            DodgeGamePhase.PAUSED -> {
                DodgePauseOverlay(
                    difficultyLabel = controller.difficulty.label,
                    score = controller.score,
                    onResume = { controller.resumeGame() },
                    onRestart = { controller.startGame(controller.difficulty) },
                    onChangeDifficulty = { controller.openDifficultySelector() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            DodgeGamePhase.GAME_OVER -> {
                DodgeGameOverOverlay(
                    score = controller.score,
                    bestScore = controller.bestScore,
                    levelReached = controller.level,
                    difficultyLabel = controller.difficulty.label,
                    onRestart = { controller.startGame(controller.difficulty) },
                    onChangeDifficulty = { controller.openDifficultySelector() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            DodgeGamePhase.PLAYING -> Unit
        }
    }
}

@Composable
private fun DodgeTopHud(
    score: Int,
    combo: Int,
    level: Int,
    difficultyLabel: String,
    lives: Int,
    maxLives: Int,
    hasShield: Boolean,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xCC1B222C)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PUNTOS",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
                Text(
                    text = score.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "NIVEL $level · $difficultyLabel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4DD0E1)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (combo > 1) {
                        Text(
                            text = "RACHA x$combo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                    if (hasShield) {
                        Text(
                            text = "ESCUDO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64B5F6)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(maxLives) { index ->
                        val active = index < lives
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .background(
                                    color = if (active) Color(0xFFEF5350) else Color(0xFF374151),
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onTogglePause,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isPaused) "▶" else "❚❚",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DodgeBottomBar(
    dashReadyRatio: Float,
    onDashClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReady = dashReadyRatio >= 0.999f

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xCC1B222C)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Arrastra para mover · Tap para Dash",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { dashReadyRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 12.dp)
                        .height(8.dp),
                    color = if (isReady) Color(0xFF4DD0E1) else Color(0xFF78909C),
                    trackColor = Color(0xFF2C3542)
                )
            }

            Button(
                onClick = onDashClick,
                enabled = isReady
            ) {
                Text(if (isReady) "DASH" else "CARGANDO")
            }
        }
    }
}

@Composable
private fun DodgeDifficultyOverlay(
    selectedDifficulty: DodgeDifficulty,
    bestScore: Int,
    onSelectDifficulty: (DodgeDifficulty) -> Unit,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xF2181E27),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "DODGE & DASH",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            if (bestScore > 0) {
                Text(
                    text = "Mejor puntaje: $bestScore",
                    fontSize = 14.sp,
                    color = Color(0xFFFFD54F)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DodgeDifficulty.entries.forEach { option ->
                    if (option == selectedDifficulty) {
                        Button(onClick = { onSelectDifficulty(option) }) {
                            Text(option.label)
                        }
                    } else {
                        OutlinedButton(onClick = { onSelectDifficulty(option) }) {
                            Text(option.label)
                        }
                    }
                }
            }

            Text(
                text = dodgeDifficultySummary(selectedDifficulty),
                fontSize = 13.sp,
                color = Color.LightGray
            )

            Button(
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Comenzar partida")
            }
        }
    }
}

private fun dodgeDifficultySummary(difficulty: DodgeDifficulty): String = when (difficulty) {
    DodgeDifficulty.EASY -> "Oleadas más espaciadas, velocidad moderada y puntos base x0.8."
    DodgeDifficulty.NORMAL -> "Ritmo equilibrado entre barreras, ondas, orbes y puntos base x1.0."
    DodgeDifficulty.HARD -> "Mayor velocidad, más obstáculos simultáneos y puntos base x1.35."
}

@Composable
private fun DodgePauseOverlay(
    difficultyLabel: String,
    score: Int,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onChangeDifficulty: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xF2181E27),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "PAUSA",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF4DD0E1)
            )
            Text(
                text = "Puntaje actual: $score · $difficultyLabel",
                fontSize = 15.sp,
                color = Color.LightGray
            )
            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reanudar")
            }
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reiniciar partida")
            }
            TextButton(onClick = onChangeDifficulty) {
                Text("Cambiar dificultad")
            }
        }
    }
}

@Composable
private fun DodgeGameOverOverlay(
    score: Int,
    bestScore: Int,
    levelReached: Int,
    difficultyLabel: String,
    onRestart: () -> Unit,
    onChangeDifficulty: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xF2181E27),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "GAME OVER",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFEF5350)
            )
            Text(
                text = "Puntaje final: $score",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "Mejor puntaje: $bestScore · Nivel $levelReached ($difficultyLabel)",
                fontSize = 14.sp,
                color = Color.LightGray
            )
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Jugar de nuevo")
            }
            TextButton(onClick = onChangeDifficulty) {
                Text("Cambiar dificultad")
            }
        }
    }
}
