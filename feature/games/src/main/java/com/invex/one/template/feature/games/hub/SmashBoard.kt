package com.invex.one.template.feature.games.hub

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random
//import androidx.activity.compose.BackHandler

// ---------------------------------------------------------------------------
// Modelos y configuración de dificultad
// ---------------------------------------------------------------------------

enum class CharacterType {
    BASIC,  // 1 golpe, velocidad media
    RUNNER, // Rápido, ágil, patrones ondulados o zigzag
    TANK,   // Varios golpes, más grande y lento
    DECOY   // Penaliza si lo aplastas
}

enum class MovementPattern {
    LINEAR_BOUNCE,
    SINE_WAVE,
    ZIGZAG_DASH
}

enum class GamePhase {
    DIFFICULTY_SELECT,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class GameDifficulty(
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
        baseSpawnInterval = 1.35f,
        spawnDecayPerLevel = 0.07f,
        baseSpeedMultiplier = 0.8f,
        speedStepPerLevel = 0.12f,
        maxActiveBase = 5,
        pointsPerLevel = 500,
        scoreMultiplier = 0.8f
    ),
    NORMAL(
        label = "Normal",
        baseSpawnInterval = 1.10f,
        spawnDecayPerLevel = 0.08f,
        baseSpeedMultiplier = 1.0f,
        speedStepPerLevel = 0.15f,
        maxActiveBase = 6,
        pointsPerLevel = 500,
        scoreMultiplier = 1.0f
    ),
    HARD(
        label = "Difícil",
        baseSpawnInterval = 0.85f,
        spawnDecayPerLevel = 0.09f,
        baseSpeedMultiplier = 1.3f,
        speedStepPerLevel = 0.18f,
        maxActiveBase = 8,
        pointsPerLevel = 650,
        scoreMultiplier = 1.35f
    )
}

enum class SmashSoundEvent {
    SMASH,
    ARMOR_HIT,
    COMBO_UP,
    PENALTY,
    GAME_OVER
}

data class SmashEntity(
    val id: Long,
    val type: CharacterType,
    val pattern: MovementPattern,
    var x: Float,
    var y: Float,
    val baseX: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    var hp: Int,
    var elapsedTime: Float = 0f,
    var waveAmplitude: Float = 0f,
    var waveFrequency: Float = 0f,
    var zigzagTimer: Float = 0f,
    var zigzagInterval: Float = 0.65f,
    var isSmashed: Boolean = false,
    var smashTimer: Float = 0f
)

data class SmashParticle(
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

data class FloatingPopup(
    val text: String,
    var x: Float,
    var y: Float,
    val vy: Float,
    val color: Color,
    val fontSizeSp: Float = 20f,
    var age: Float = 0f,
    val maxAge: Float = 0.75f
) {
    val progress: Float get() = (age / maxAge).coerceIn(0f, 1f)
    val isExpired: Boolean get() = age >= maxAge
}

data class LevelStepConfig(
    val level: Int,
    val spawnIntervalSeconds: Float,
    val speedMultiplier: Float,
    val maxActiveEntities: Int
)

fun configForLevel(difficulty: GameDifficulty, level: Int): LevelStepConfig {
    val clampedLevel = max(1, level)
    val levelOffset = clampedLevel - 1
    return LevelStepConfig(
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

class SmashSoundPoolPlayer(
    context: Context,
    private val soundResMap: Map<SmashSoundEvent, Int>
) {
    private val soundPool: SoundPool
    private val loadedIds = mutableMapOf<SmashSoundEvent, Int>()
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

    fun play(event: SmashSoundEvent, pitchRate: Float = 1.0f) {
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
fun rememberSmashSoundPlayer(
    soundResMap: Map<SmashSoundEvent, Int>
): SmashSoundPoolPlayer {
    val context = LocalContext.current.applicationContext
    val player = remember(soundResMap) {
        SmashSoundPoolPlayer(context, soundResMap)
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    return player
}

// ---------------------------------------------------------------------------
// Controlador principal del juego
// ---------------------------------------------------------------------------

class SmashGameController(
    val initialLives: Int = 3,
    initialDifficulty: GameDifficulty = GameDifficulty.NORMAL,
    startOnDifficultySelect: Boolean = true,
    private val onSoundEvent: ((SmashSoundEvent, Float) -> Unit)? = null
) {
    var difficulty by mutableStateOf(initialDifficulty)
        private set
    var phase by mutableStateOf(
        if (startOnDifficultySelect) GamePhase.DIFFICULTY_SELECT else GamePhase.PLAYING
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
    var frameTick by mutableLongStateOf(0L)
        private set

    private val entities = mutableListOf<SmashEntity>()
    private val particles = mutableListOf<SmashParticle>()
    private val popups = mutableListOf<FloatingPopup>()

    private var nextId = 1L
    private var spawnAccumulator = 0f
    private var boardWidth = 0f
    private var boardHeight = 0f

    fun updateBoardSize(width: Float, height: Float) {
        boardWidth = width
        boardHeight = height
    }

    fun selectDifficulty(selected: GameDifficulty) {
        difficulty = selected
    }

    fun startGame(selectedDifficulty: GameDifficulty = difficulty) {
        difficulty = selectedDifficulty
        entities.clear()
        particles.clear()
        popups.clear()
        score = 0
        combo = 0
        level = 1
        lives = initialLives
        spawnAccumulator = 0f
        phase = GamePhase.PLAYING
        frameTick++
    }

    fun openDifficultySelector() {
        phase = GamePhase.DIFFICULTY_SELECT
        frameTick++
    }

    fun pauseGame() {
        if (phase == GamePhase.PLAYING) {
            phase = GamePhase.PAUSED
            frameTick++
        }
    }

    fun resumeGame() {
        if (phase == GamePhase.PAUSED) {
            phase = GamePhase.PLAYING
            frameTick++
        }
    }

    fun togglePause() {
        when (phase) {
            GamePhase.PLAYING -> pauseGame()
            GamePhase.PAUSED -> resumeGame()
            else -> Unit
        }
    }

    fun step(deltaSeconds: Float) {
        if (boardWidth <= 0f || boardHeight <= 0f) return

        // Congela simulación y partículas en DIFFICULTY_SELECT y PAUSED
        if (phase == GamePhase.DIFFICULTY_SELECT || phase == GamePhase.PAUSED) {
            return
        }

        updateParticlesAndPopups(deltaSeconds)

        if (phase != GamePhase.PLAYING) {
            frameTick++
            return
        }

        level = 1 + (score / difficulty.pointsPerLevel)
        val config = configForLevel(difficulty, level)

        spawnAccumulator += deltaSeconds
        if (spawnAccumulator >= config.spawnIntervalSeconds && entities.size < config.maxActiveEntities) {
            spawnAccumulator = 0f
            spawnEntity(config.speedMultiplier)
        }

        val iterator = entities.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            if (entity.isSmashed) {
                entity.smashTimer -= deltaSeconds
                if (entity.smashTimer <= 0f) iterator.remove()
                continue
            }

            updateEntityMovement(entity, deltaSeconds)

            if (entity.y - entity.radius > boardHeight) {
                if (entity.type != CharacterType.DECOY) {
                    combo = 0
                    loseLife()
                }
                iterator.remove()
                if (phase == GamePhase.GAME_OVER) break
            }
        }

        frameTick++
    }

    private fun updateEntityMovement(entity: SmashEntity, deltaSeconds: Float) {
        entity.elapsedTime += deltaSeconds

        when (entity.pattern) {
            MovementPattern.LINEAR_BOUNCE -> {
                entity.x += entity.vx * deltaSeconds
                entity.y += entity.vy * deltaSeconds
                bounceHorizontal(entity)
            }

            MovementPattern.SINE_WAVE -> {
                entity.y += entity.vy * deltaSeconds
                val waveOffset = sin(entity.elapsedTime * entity.waveFrequency) * entity.waveAmplitude
                entity.x = (entity.baseX + waveOffset).coerceIn(
                    entity.radius,
                    max(entity.radius, boardWidth - entity.radius)
                )
            }

            MovementPattern.ZIGZAG_DASH -> {
                entity.zigzagTimer += deltaSeconds
                if (entity.zigzagTimer >= entity.zigzagInterval) {
                    entity.zigzagTimer = 0f
                    entity.vx = -entity.vx * 1.15f
                }
                entity.x += entity.vx * deltaSeconds
                entity.y += entity.vy * deltaSeconds
                bounceHorizontal(entity)
            }
        }
    }

    private fun bounceHorizontal(entity: SmashEntity) {
        if (entity.x - entity.radius < 0f) {
            entity.x = entity.radius
            entity.vx = abs(entity.vx)
        } else if (entity.x + entity.radius > boardWidth) {
            entity.x = boardWidth - entity.radius
            entity.vx = -abs(entity.vx)
        }
    }

    private fun updateParticlesAndPopups(deltaSeconds: Float) {
        val particleIt = particles.iterator()
        while (particleIt.hasNext()) {
            val p = particleIt.next()
            p.age += deltaSeconds
            if (p.isExpired) {
                particleIt.remove()
                continue
            }
            p.x += p.vx * deltaSeconds
            p.y += p.vy * deltaSeconds
            p.vx *= (1f - 3.5f * deltaSeconds).coerceAtLeast(0f)
            p.vy *= (1f - 3.5f * deltaSeconds).coerceAtLeast(0f)
        }

        val popupIt = popups.iterator()
        while (popupIt.hasNext()) {
            val popup = popupIt.next()
            popup.age += deltaSeconds
            if (popup.isExpired) {
                popupIt.remove()
                continue
            }
            popup.y += popup.vy * deltaSeconds
        }
    }

    private fun loseLife() {
        lives = max(0, lives - 1)
        if (lives == 0) {
            triggerGameOver()
        }
    }

    private fun triggerGameOver() {
        phase = GamePhase.GAME_OVER
        if (score > bestScore) {
            bestScore = score
        }
        onSoundEvent?.invoke(SmashSoundEvent.GAME_OVER, 1.0f)
    }

    fun onTap(tapOffset: Offset): Boolean {
        if (phase != GamePhase.PLAYING) return false

        for (i in entities.lastIndex downTo 0) {
            val entity = entities[i]
            if (entity.isSmashed) continue

            val distance = hypot(tapOffset.x - entity.x, tapOffset.y - entity.y)
            if (distance <= entity.radius * 1.15f) {
                handleHit(entity)
                frameTick++
                return true
            }
        }
        return false
    }

    private fun handleHit(entity: SmashEntity) {
        val baseColor = colorForType(entity.type)

        when (entity.type) {
            CharacterType.DECOY -> {
                combo = 0
                val penalty = (100 * difficulty.scoreMultiplier).toInt().coerceAtLeast(50)
                score = max(0, score - penalty)
                entity.isSmashed = true
                entity.smashTimer = 0.25f

                onSoundEvent?.invoke(SmashSoundEvent.PENALTY, 0.85f)
                spawnBurst(
                    x = entity.x,
                    y = entity.y,
                    color = Color(0xFFEF5350),
                    count = 10,
                    maxSpeed = 240f
                )
                popups.add(
                    FloatingPopup(
                        text = "-$penalty",
                        x = entity.x,
                        y = entity.y - 12f,
                        vy = -90f,
                        color = Color(0xFFEF5350),
                        fontSizeSp = 22f
                    )
                )
            }

            else -> {
                entity.hp -= 1
                if (entity.hp <= 0) {
                    val previousMultiplier = 1 + (combo / 5)
                    combo += 1
                    val newMultiplier = 1 + (combo / 5)
                    val pointsEarned = (100 * newMultiplier * difficulty.scoreMultiplier)
                        .toInt()
                        .coerceAtLeast(10)
                    score += pointsEarned
                    if (score > bestScore) bestScore = score

                    entity.isSmashed = true
                    entity.smashTimer = 0.2f

                    val pitch = (0.95f + (combo % 10) * 0.03f).coerceAtMost(1.35f)
                    onSoundEvent?.invoke(SmashSoundEvent.SMASH, pitch)
                    if (newMultiplier > previousMultiplier) {
                        onSoundEvent?.invoke(SmashSoundEvent.COMBO_UP, 1.15f)
                    }

                    spawnBurst(
                        x = entity.x,
                        y = entity.y,
                        color = baseColor,
                        count = 14,
                        maxSpeed = 340f
                    )
                    popups.add(
                        FloatingPopup(
                            text = "+$pointsEarned",
                            x = entity.x,
                            y = entity.y - 10f,
                            vy = -125f,
                            color = Color(0xFFFFF59D),
                            fontSizeSp = 22f
                        )
                    )
                    if (combo >= 2) {
                        popups.add(
                            FloatingPopup(
                                text = "x$combo",
                                x = entity.x + 28f,
                                y = entity.y - 36f,
                                vy = -105f,
                                color = Color(0xFFFFB74D),
                                fontSizeSp = 17f,
                                maxAge = 0.85f
                            )
                        )
                    }
                } else {
                    onSoundEvent?.invoke(SmashSoundEvent.ARMOR_HIT, 0.9f)
                    spawnBurst(
                        x = entity.x,
                        y = entity.y,
                        color = Color.White,
                        count = 6,
                        maxSpeed = 180f
                    )
                    popups.add(
                        FloatingPopup(
                            text = "HP ${entity.hp}",
                            x = entity.x,
                            y = entity.y - 12f,
                            vy = -85f,
                            color = Color(0xFFE1BEE7),
                            fontSizeSp = 16f,
                            maxAge = 0.55f
                        )
                    )
                }
            }
        }
    }

    private fun spawnBurst(
        x: Float,
        y: Float,
        color: Color,
        count: Int,
        maxSpeed: Float
    ) {
        if (particles.size >= 260) return
        val allowedCount = minOf(count, 260 - particles.size)

        for (i in 0 until allowedCount) {
            val angle = (2.0 * PI * i / allowedCount) + (Random.nextFloat() * 0.45 - 0.225)
            val speed = Random.nextFloat() * maxSpeed * 0.6f + maxSpeed * 0.4f
            val radius = Random.nextFloat() * 6f + 4f

            particles.add(
                SmashParticle(
                    x = x,
                    y = y,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    radius = radius,
                    color = color,
                    maxAge = Random.nextFloat() * 0.2f + 0.35f
                )
            )
        }
    }

    private fun colorForType(type: CharacterType): Color = when (type) {
        CharacterType.BASIC -> Color(0xFF4CAF50)
        CharacterType.RUNNER -> Color(0xFFFF9800)
        CharacterType.TANK -> Color(0xFF9C27B0)
        CharacterType.DECOY -> Color(0xFF2196F3)
    }

    private fun spawnEntity(speedMultiplier: Float) {
        val roll = Random.nextFloat()
        val type = when {
            roll < 0.55f -> CharacterType.BASIC
            roll < 0.80f -> CharacterType.RUNNER
            roll < 0.92f -> CharacterType.TANK
            else -> CharacterType.DECOY
        }

        val pattern = when (type) {
            CharacterType.RUNNER -> if (Random.nextFloat() < 0.65f) {
                MovementPattern.SINE_WAVE
            } else {
                MovementPattern.ZIGZAG_DASH
            }
            CharacterType.BASIC -> if (Random.nextFloat() < 0.35f) {
                MovementPattern.SINE_WAVE
            } else {
                MovementPattern.LINEAR_BOUNCE
            }
            CharacterType.TANK, CharacterType.DECOY -> MovementPattern.LINEAR_BOUNCE
        }

        val radius = when (type) {
            CharacterType.TANK -> 68f
            CharacterType.RUNNER -> 42f
            CharacterType.BASIC, CharacterType.DECOY -> 54f
        }

        val baseSpeed = when (type) {
            CharacterType.RUNNER -> 290f
            CharacterType.TANK -> 110f
            CharacterType.BASIC, CharacterType.DECOY -> 185f
        } * speedMultiplier

        val safeSpan = (boardWidth - radius * 2).coerceAtLeast(1f)
        val startX = Random.nextFloat() * safeSpan + radius
        val horizontalDir = if (Random.nextBoolean()) 1f else -1f
        val horizontalSpeed = (Random.nextFloat() * 90f + 70f) * horizontalDir * speedMultiplier.coerceAtMost(1.8f)
        val hp = if (type == CharacterType.TANK) 3 else 1

        entities.add(
            SmashEntity(
                id = nextId++,
                type = type,
                pattern = pattern,
                x = startX,
                y = -radius,
                baseX = startX,
                vx = horizontalSpeed,
                vy = baseSpeed,
                radius = radius,
                hp = hp,
                waveAmplitude = if (pattern == MovementPattern.SINE_WAVE) Random.nextFloat() * 45f + 35f else 0f,
                waveFrequency = if (pattern == MovementPattern.SINE_WAVE) Random.nextFloat() * 3f + 4f else 0f,
                zigzagInterval = Random.nextFloat() * 0.35f + 0.45f
            )
        )
    }

    fun renderEntities(
        drawScope: DrawScope,
        textMeasurer: TextMeasurer
    ) {
        val unused = frameTick
        with(drawScope) {
            for (entity in entities) {
                drawCircle(
                    color = colorForType(entity.type),
                    radius = if (entity.isSmashed) entity.radius * 1.15f else entity.radius,
                    center = Offset(entity.x, entity.y),
                    alpha = if (entity.isSmashed) 0.45f else 1f
                )
            }

            for (p in particles) {
                val alpha = 1f - p.progress
                val currentRadius = p.radius * (1f - 0.5f * p.progress)
                drawCircle(
                    color = p.color,
                    radius = currentRadius,
                    center = Offset(p.x, p.y),
                    alpha = alpha
                )
            }

            for (popup in popups) {
                val alpha = 1f - popup.progress
                val layoutResult = textMeasurer.measure(
                    text = popup.text,
                    style = TextStyle(
                        color = popup.color.copy(alpha = alpha),
                        fontSize = popup.fontSizeSp.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                val maxX = (size.width - layoutResult.size.width - 16f).coerceAtLeast(16f)
                val topLeft = Offset(
                    x = (popup.x - layoutResult.size.width / 2f).coerceIn(16f, maxX),
                    y = (popup.y - layoutResult.size.height / 2f).coerceAtLeast(16f)
                )
                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = topLeft
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Pantalla principal, HUD y overlays (Dificultad, Pausa y Game Over)
// ---------------------------------------------------------------------------

@Composable
fun SmashGameScreen(
    modifier: Modifier = Modifier,
    soundResMap: Map<SmashSoundEvent, Int> = emptyMap(),
    initialLives: Int = 3,
    initialDifficulty: GameDifficulty = GameDifficulty.NORMAL,
) {
    val haptics = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()
    val lifecycleOwner = LocalLifecycleOwner.current
    val soundPlayer = rememberSmashSoundPlayer(soundResMap = soundResMap)

    val controller = remember(initialLives, initialDifficulty) {
        SmashGameController(
            initialLives = initialLives,
            initialDifficulty = initialDifficulty,
            startOnDifficultySelect = true,
            onSoundEvent = { event, pitch ->
                soundPlayer.play(event = event, pitchRate = pitch)
            }
        )
    }

    // Dentro de SmashGameScreen, justo junto a los efectos de ciclo de vida / game loop:
    //val handleBackInGame = controller.phase == GamePhase.PLAYING || controller.phase == GamePhase.PAUSED
    //BackHandler(enabled = handleBackInGame) {
    //    controller.togglePause()
    //}

    // Pausa automática si la app pasa a segundo plano
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
                    val rawDeltaSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                    val clampedDelta = rawDeltaSeconds.coerceAtMost(0.05f)
                    controller.step(clampedDelta)
                }
                lastFrameTimeNanos = frameTimeNanos
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(controller) {
                    detectTapGestures { offset ->
                        if (controller.onTap(offset)) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                }
        ) {
            controller.updateBoardSize(size.width, size.height)
            controller.renderEntities(this, textMeasurer)
        }

        if (controller.phase != GamePhase.DIFFICULTY_SELECT) {
            SmashHud(
                score = controller.score,
                combo = controller.combo,
                level = controller.level,
                difficultyLabel = controller.difficulty.label,
                lives = controller.lives,
                maxLives = controller.initialLives,
                isPaused = controller.phase == GamePhase.PAUSED,
                onTogglePause = { controller.togglePause() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(12.dp)
            )
        }

        when (controller.phase) {
            GamePhase.DIFFICULTY_SELECT -> {
                DifficultySelectOverlay(
                    selectedDifficulty = controller.difficulty,
                    bestScore = controller.bestScore,
                    onSelectDifficulty = { controller.selectDifficulty(it) },
                    onStartGame = { controller.startGame(controller.difficulty) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            GamePhase.PAUSED -> {
                PauseOverlay(
                    difficultyLabel = controller.difficulty.label,
                    score = controller.score,
                    onResume = { controller.resumeGame() },
                    onRestart = { controller.startGame(controller.difficulty) },
                    onChangeDifficulty = { controller.openDifficultySelector() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            GamePhase.GAME_OVER -> {
                GameOverOverlay(
                    score = controller.score,
                    bestScore = controller.bestScore,
                    levelReached = controller.level,
                    difficultyLabel = controller.difficulty.label,
                    onRestart = { controller.startGame(controller.difficulty) },
                    onChangeDifficulty = { controller.openDifficultySelector() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            GamePhase.PLAYING -> Unit
        }
    }
}

@Composable
private fun SmashHud(
    score: Int,
    combo: Int,
    level: Int,
    difficultyLabel: String,
    lives: Int,
    maxLives: Int,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xCC1E1E1E)
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
                    color = Color(0xFF81C784)
                )
                if (combo > 1) {
                    Text(
                        text = "COMBO x$combo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D)
                    )
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
                                    color = if (active) Color(0xFFEF5350) else Color(0xFF424242),
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
private fun DifficultySelectOverlay(
    selectedDifficulty: GameDifficulty,
    bestScore: Int,
    onSelectDifficulty: (GameDifficulty) -> Unit,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xF21C1C1C),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "ELIGE DIFICULTAD",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            if (bestScore > 0) {
                Text(
                    text = "Mejor puntaje: $bestScore",
                    fontSize = 14.sp,
                    color = Color(0xFFFFB74D)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameDifficulty.entries.forEach { option ->
                    val isSelected = option == selectedDifficulty
                    if (isSelected) {
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
                text = difficultyDescription(selectedDifficulty),
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

private fun difficultyDescription(difficulty: GameDifficulty): String = when (difficulty) {
    GameDifficulty.EASY -> "Velocidad relajada, más espacio entre oleadas y puntos base x0.8."
    GameDifficulty.NORMAL -> "Ritmo equilibrado con avance progresivo y puntos base x1.0."
    GameDifficulty.HARD -> "Mayor velocidad, más enemigos en pantalla y puntos base x1.35."
}

@Composable
private fun PauseOverlay(
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
        color = Color(0xF21C1C1C),
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
                color = Color(0xFFFFB74D)
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
private fun GameOverOverlay(
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
        color = Color(0xF01C1C1C),
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
