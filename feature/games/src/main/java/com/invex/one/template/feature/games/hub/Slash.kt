package com.invex.one.template.feature.games.hub

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
//import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Modelos y configuración de dificultad
// ---------------------------------------------------------------------------

enum class SlashGamePhase {
    DIFFICULTY_SELECT,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class SlashDifficulty(
    val label: String,
    val baseSpawnInterval: Float,
    val spawnDecayPerLevel: Float,
    val baseLaunchSpeedMultiplier: Float,
    val launchStepPerLevel: Float,
    val maxActiveBase: Int,
    val pointsPerLevel: Int,
    val scoreMultiplier: Float
) {
    EASY(
        label = "Fácil",
        baseSpawnInterval = 1.25f,
        spawnDecayPerLevel = 0.06f,
        baseLaunchSpeedMultiplier = 0.88f,
        launchStepPerLevel = 0.06f,
        maxActiveBase = 5,
        pointsPerLevel = 500,
        scoreMultiplier = 0.8f
    ),
    NORMAL(
        label = "Normal",
        baseSpawnInterval = 0.95f,
        spawnDecayPerLevel = 0.07f,
        baseLaunchSpeedMultiplier = 1.0f,
        launchStepPerLevel = 0.07f,
        maxActiveBase = 7,
        pointsPerLevel = 500,
        scoreMultiplier = 1.0f
    ),
    HARD(
        label = "Difícil",
        baseSpawnInterval = 0.70f,
        spawnDecayPerLevel = 0.08f,
        baseLaunchSpeedMultiplier = 1.18f,
        launchStepPerLevel = 0.08f,
        maxActiveBase = 10,
        pointsPerLevel = 650,
        scoreMultiplier = 1.35f
    )
}

enum class SlashSoundEvent {
    SLASH_HIT,
    MULTI_SLASH,
    BOMB_HIT,
    MISS_LIFE,
    GAME_OVER
}

enum class SlashTargetKind {
    BASIC, // 1 corte
    FAST,  // Más pequeño y veloz
    HEAVY, // Requiere 2 cortes para destruirse
    BOMB   // Penaliza si lo cortas
}

data class SlashEntity(
    val id: Long,
    val kind: SlashTargetKind,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    var hp: Int,
    var rotation: Float = 0f,
    val angularVelocity: Float = 0f,
    var hasEnteredScreen: Boolean = false,
    var isSliced: Boolean = false,
    var sliceTimer: Float = 0f
)

data class BladePoint(
    val offset: Offset,
    var age: Float = 0f,
    val maxAge: Float = 0.16f
) {
    val isExpired: Boolean get() = age >= maxAge
    val alpha: Float get() = (1f - (age / maxAge).coerceIn(0f, 1f))
}

data class SlashParticle(
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

data class SlashPopup(
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

data class SlashLevelConfig(
    val level: Int,
    val spawnIntervalSeconds: Float,
    val launchSpeedMultiplier: Float,
    val maxActiveEntities: Int
)

fun configForSlashLevel(difficulty: SlashDifficulty, level: Int): SlashLevelConfig {
    val clampedLevel = max(1, level)
    val levelOffset = clampedLevel - 1
    return SlashLevelConfig(
        level = clampedLevel,
        spawnIntervalSeconds = max(
            0.24f,
            difficulty.baseSpawnInterval - levelOffset * difficulty.spawnDecayPerLevel
        ),
        launchSpeedMultiplier = difficulty.baseLaunchSpeedMultiplier + levelOffset * difficulty.launchStepPerLevel,
        maxActiveEntities = (difficulty.maxActiveBase + levelOffset * 2).coerceAtMost(28)
    )
}

// ---------------------------------------------------------------------------
// Reproductor de sonido con SoundPool (baja latencia en Android)
// ---------------------------------------------------------------------------

class SlashSoundPoolPlayer(
    context: Context,
    private val soundResMap: Map<SlashSoundEvent, Int>
) {
    private val soundPool: SoundPool
    private val loadedIds = mutableMapOf<SlashSoundEvent, Int>()
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

    fun play(event: SlashSoundEvent, pitchRate: Float = 1.0f) {
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
fun rememberSlashSoundPlayer(
    soundResMap: Map<SlashSoundEvent, Int>
): SlashSoundPoolPlayer {
    val context = LocalContext.current.applicationContext
    val player = remember(soundResMap) {
        SlashSoundPoolPlayer(context, soundResMap)
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    return player
}

// ---------------------------------------------------------------------------
// Controlador principal de Swipe & Slash
// ---------------------------------------------------------------------------

class SlashGameController(
    val initialLives: Int = 3,
    initialDifficulty: SlashDifficulty = SlashDifficulty.NORMAL,
    startOnDifficultySelect: Boolean = true,
    private val onSoundEvent: ((SlashSoundEvent, Float) -> Unit)? = null
) {
    var difficulty by mutableStateOf(initialDifficulty)
        private set
    var phase by mutableStateOf(
        if (startOnDifficultySelect) SlashGamePhase.DIFFICULTY_SELECT else SlashGamePhase.PLAYING
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

    private var boardWidth = 0f
    private var boardHeight = 0f
    private val gravity = 980f

    private val entities = mutableListOf<SlashEntity>()
    private val bladeTrail = mutableListOf<BladePoint>()
    private val particles = mutableListOf<SlashParticle>()
    private val popups = mutableListOf<SlashPopup>()

    private var nextId = 1L
    private var spawnAccumulator = 0f
    private var lastDragPoint: Offset? = null
    private var currentStrokeSlices = 0

    fun updateBoardSize(width: Float, height: Float) {
        boardWidth = width
        boardHeight = height
    }

    fun selectDifficulty(selected: SlashDifficulty) {
        difficulty = selected
    }

    fun startGame(selectedDifficulty: SlashDifficulty = difficulty) {
        difficulty = selectedDifficulty
        entities.clear()
        bladeTrail.clear()
        particles.clear()
        popups.clear()

        score = 0
        combo = 0
        level = 1
        lives = initialLives
        spawnAccumulator = 0f
        lastDragPoint = null
        currentStrokeSlices = 0

        phase = SlashGamePhase.PLAYING
        frameTick++
    }

    fun openDifficultySelector() {
        lastDragPoint = null
        currentStrokeSlices = 0
        phase = SlashGamePhase.DIFFICULTY_SELECT
        frameTick++
    }

    fun pauseGame() {
        if (phase == SlashGamePhase.PLAYING) {
            lastDragPoint = null
            currentStrokeSlices = 0
            phase = SlashGamePhase.PAUSED
            frameTick++
        }
    }

    fun resumeGame() {
        if (phase == SlashGamePhase.PAUSED) {
            phase = SlashGamePhase.PLAYING
            frameTick++
        }
    }

    fun togglePause() {
        when (phase) {
            SlashGamePhase.PLAYING -> pauseGame()
            SlashGamePhase.PAUSED -> resumeGame()
            else -> Unit
        }
    }

    fun onDragStart(offset: Offset) {
        if (phase != SlashGamePhase.PLAYING) return
        lastDragPoint = offset
        currentStrokeSlices = 0
        bladeTrail.add(BladePoint(offset = offset, maxAge = 0.16f))
        frameTick++
    }

    /**
     * Procesa un segmento de arrastre y devuelve true si cortó algún objetivo.
     */
    fun onDragMove(currentOffset: Offset): Boolean {
        if (phase != SlashGamePhase.PLAYING) return false

        val previous = lastDragPoint
        lastDragPoint = currentOffset
        bladeTrail.add(BladePoint(offset = currentOffset, maxAge = 0.16f))

        if (previous == null) {
            frameTick++
            return false
        }

        var hitSomething = false
        for (i in entities.lastIndex downTo 0) {
            val entity = entities[i]
            if (entity.isSliced) continue

            val dist = distancePointToSegment(
                point = Offset(entity.x, entity.y),
                segStart = previous,
                segEnd = currentOffset
            )
            if (dist <= entity.radius * 1.12f) {
                if (handleSlice(entity, currentOffset)) {
                    hitSomething = true
                }
            }
        }

        frameTick++
        return hitSomething
    }

    fun onDragEnd() {
        if (phase == SlashGamePhase.PLAYING && currentStrokeSlices >= 3) {
            val bonus = (150 * (1 + combo / 5) * difficulty.scoreMultiplier)
                .toInt()
                .coerceAtLeast(50)
            addScore(bonus)
            onSoundEvent?.invoke(SlashSoundEvent.MULTI_SLASH, 1.25f)

            val anchor = lastDragPoint ?: Offset(boardWidth / 2f, boardHeight / 2f)
            popups.add(
                SlashPopup(
                    text = "COMBO x$currentStrokeSlices +$bonus",
                    x = anchor.x,
                    y = anchor.y - 24f,
                    vy = -110f,
                    color = Color(0xFF4DD0E1),
                    fontSizeSp = 21f,
                    maxAge = 0.9f
                )
            )
        }
        lastDragPoint = null
        currentStrokeSlices = 0
        frameTick++
    }

    fun step(deltaSeconds: Float) {
        if (boardWidth <= 0f || boardHeight <= 0f) return
        if (phase == SlashGamePhase.DIFFICULTY_SELECT || phase == SlashGamePhase.PAUSED) {
            return
        }

        updateTrailParticlesAndPopups(deltaSeconds)

        if (phase != SlashGamePhase.PLAYING) {
            frameTick++
            return
        }

        level = 1 + (score / difficulty.pointsPerLevel)
        val config = configForSlashLevel(difficulty, level)

        spawnAccumulator += deltaSeconds
        if (spawnAccumulator >= config.spawnIntervalSeconds && entities.size < config.maxActiveEntities) {
            spawnAccumulator = 0f
            spawnEntity(config.launchSpeedMultiplier)
        }

        val iterator = entities.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            if (entity.isSliced) {
                entity.sliceTimer -= deltaSeconds
                if (entity.sliceTimer <= 0f) iterator.remove()
                continue
            }

            entity.x += entity.vx * deltaSeconds
            entity.y += entity.vy * deltaSeconds
            entity.vy += gravity * deltaSeconds
            entity.rotation += entity.angularVelocity * deltaSeconds

            if (entity.y < boardHeight - entity.radius) {
                entity.hasEnteredScreen = true
            }

            // Si ya subió a escena y vuelve a caer por debajo del tablero sin cortarse
            if (entity.hasEnteredScreen && entity.vy > 0f && entity.y - entity.radius > boardHeight) {
                if (entity.kind != SlashTargetKind.BOMB) {
                    combo = 0
                    loseLife(
                        x = entity.x.coerceIn(40f, max(40f, boardWidth - 40f)),
                        y = boardHeight - 48f
                    )
                }
                iterator.remove()
                if (phase != SlashGamePhase.PLAYING) break
            }
        }

        frameTick++
    }

    private fun updateTrailParticlesAndPopups(deltaSeconds: Float) {
        val trailIt = bladeTrail.iterator()
        while (trailIt.hasNext()) {
            val pt = trailIt.next()
            pt.age += deltaSeconds
            if (pt.isExpired) trailIt.remove()
        }

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
            p.vx *= (1f - 3.2f * deltaSeconds).coerceAtLeast(0f)
            p.vy *= (1f - 3.2f * deltaSeconds).coerceAtLeast(0f)
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

    private fun handleSlice(entity: SlashEntity, hitPoint: Offset): Boolean {
        when (entity.kind) {
            SlashTargetKind.BOMB -> {
                entity.isSliced = true
                entity.sliceTimer = 0.25f
                combo = 0
                currentStrokeSlices = 0
                spawnBurst(x = entity.x, y = entity.y, color = Color(0xFFFF5252), count = 18, maxSpeed = 360f)
                loseLife(x = entity.x, y = entity.y, customSound = SlashSoundEvent.BOMB_HIT)
                return true
            }

            else -> {
                entity.hp -= 1
                if (entity.hp <= 0) {
                    entity.isSliced = true
                    entity.sliceTimer = 0.18f
                    combo += 1
                    currentStrokeSlices += 1

                    val multiplier = 1 + (combo / 5)
                    val basePts = when (entity.kind) {
                        SlashTargetKind.FAST -> 140
                        SlashTargetKind.HEAVY -> 200
                        SlashTargetKind.BASIC -> 100
                        SlashTargetKind.BOMB -> 0
                    }
                    val earned = (basePts * multiplier * difficulty.scoreMultiplier)
                        .toInt()
                        .coerceAtLeast(25)
                    addScore(earned)

                    val pitch = (0.95f + (combo % 10) * 0.035f).coerceAtMost(1.4f)
                    onSoundEvent?.invoke(SlashSoundEvent.SLASH_HIT, pitch)
                    spawnBurst(
                        x = entity.x,
                        y = entity.y,
                        color = colorForKind(entity.kind),
                        count = 14,
                        maxSpeed = 320f
                    )
                    popups.add(
                        SlashPopup(
                            text = "+$earned",
                            x = hitPoint.x,
                            y = hitPoint.y - 12f,
                            vy = -120f,
                            color = Color(0xFFFFF59D)
                        )
                    )
                } else {
                    // Primer corte sobre objetivo blindado (HEAVY)
                    entity.vy = min(entity.vy, -240f)
                    val pitch = 0.85f
                    onSoundEvent?.invoke(SlashSoundEvent.SLASH_HIT, pitch)
                    spawnBurst(x = hitPoint.x, y = hitPoint.y, color = Color.White, count = 7, maxSpeed = 200f)
                    popups.add(
                        SlashPopup(
                            text = "1 MÁS!",
                            x = hitPoint.x,
                            y = hitPoint.y - 12f,
                            vy = -90f,
                            color = Color(0xFFCE93D8),
                            fontSizeSp = 16f,
                            maxAge = 0.55f
                        )
                    )
                }
                return true
            }
        }
    }

    private fun loseLife(
        x: Float,
        y: Float,
        customSound: SlashSoundEvent? = null
    ) {
        lives = max(0, lives - 1)
        popups.add(
            SlashPopup(
                text = "-1 VIDA",
                x = x,
                y = y,
                vy = -95f,
                color = Color(0xFFEF5350)
            )
        )
        if (lives == 0) {
            phase = SlashGamePhase.GAME_OVER
            if (score > bestScore) bestScore = score
            onSoundEvent?.invoke(SlashSoundEvent.GAME_OVER, 1.0f)
        } else {
            onSoundEvent?.invoke(customSound ?: SlashSoundEvent.MISS_LIFE, 0.9f)
        }
    }

    private fun addScore(points: Int) {
        score += points
        if (score > bestScore) {
            bestScore = score
        }
    }

    private fun spawnEntity(launchSpeedMultiplier: Float) {
        val roll = Random.nextFloat()
        val kind = when {
            roll < 0.45f -> SlashTargetKind.BASIC
            roll < 0.68f -> SlashTargetKind.FAST
            roll < 0.84f -> SlashTargetKind.HEAVY
            else -> SlashTargetKind.BOMB
        }

        val radius = when (kind) {
            SlashTargetKind.FAST -> 34f
            SlashTargetKind.HEAVY -> 54f
            SlashTargetKind.BASIC, SlashTargetKind.BOMB -> 44f
        }

        val margin = radius + 24f
        val span = (boardWidth - margin * 2).coerceAtLeast(1f)
        val startX = Random.nextFloat() * span + margin
        val startY = boardHeight + radius + 12f

        // Apunta suavemente hacia el centro horizontal para mantener la parábola dentro del tablero
        val targetCenterX = boardWidth * (0.25f + Random.nextFloat() * 0.5f)
        val flightTime = (Random.nextFloat() * 0.45f + 1.15f) / launchSpeedMultiplier.coerceAtLeast(0.6f)
        val vx = ((targetCenterX - startX) / flightTime).coerceIn(-280f, 280f)

        val baseVy = when (kind) {
            SlashTargetKind.FAST -> Random.nextFloat() * 140f + 860f
            SlashTargetKind.HEAVY -> Random.nextFloat() * 100f + 740f
            SlashTargetKind.BASIC, SlashTargetKind.BOMB -> Random.nextFloat() * 120f + 800f
        } * launchSpeedMultiplier

        val hp = if (kind == SlashTargetKind.HEAVY) 2 else 1
        val angularVelocity = (Random.nextFloat() * 6f - 3f)

        entities.add(
            SlashEntity(
                id = nextId++,
                kind = kind,
                x = startX,
                y = startY,
                vx = vx,
                vy = -baseVy,
                radius = radius,
                hp = hp,
                angularVelocity = angularVelocity
            )
        )
    }

    private fun spawnBurst(x: Float, y: Float, color: Color, count: Int, maxSpeed: Float) {
        if (particles.size >= 260) return
        val allowed = minOf(count, 260 - particles.size)
        for (i in 0 until allowed) {
            val angle = (2.0 * PI * i / allowed) + (Random.nextFloat() * 0.45 - 0.225)
            val speed = Random.nextFloat() * maxSpeed * 0.6f + maxSpeed * 0.4f
            particles.add(
                SlashParticle(
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

    private fun colorForKind(kind: SlashTargetKind): Color = when (kind) {
        SlashTargetKind.BASIC -> Color(0xFF66BB6A)
        SlashTargetKind.FAST -> Color(0xFFFFA726)
        SlashTargetKind.HEAVY -> Color(0xFFAB47BC)
        SlashTargetKind.BOMB -> Color(0xFF37474F)
    }

    private fun distancePointToSegment(point: Offset, segStart: Offset, segEnd: Offset): Float {
        val dx = segEnd.x - segStart.x
        val dy = segEnd.y - segStart.y
        val lengthSq = dx * dx + dy * dy
        if (lengthSq <= 0.0001f) {
            return hypot(point.x - segStart.x, point.y - segStart.y)
        }
        val t = (((point.x - segStart.x) * dx + (point.y - segStart.y) * dy) / lengthSq).coerceIn(0f, 1f)
        val projX = segStart.x + t * dx
        val projY = segStart.y + t * dy
        return hypot(point.x - projX, point.y - projY)
    }

    fun renderScene(drawScope: DrawScope, textMeasurer: TextMeasurer) {
        val unused = frameTick
        with(drawScope) {
            // 1. Objetivos en vuelo
            for (entity in entities) {
                val color = colorForKind(entity.kind)
                val alpha = if (entity.isSliced) 0.35f else 1f
                drawCircle(
                    color = color,
                    radius = if (entity.isSliced) entity.radius * 1.15f else entity.radius,
                    center = Offset(entity.x, entity.y),
                    alpha = alpha
                )
                if (entity.kind == SlashTargetKind.BOMB && !entity.isSliced) {
                    drawCircle(
                        color = Color(0xFFFF5252),
                        radius = entity.radius * 0.38f,
                        center = Offset(entity.x, entity.y)
                    )
                }
            }

            // 2. Estela de corte
            if (bladeTrail.size >= 2) {
                for (i in 1 until bladeTrail.size) {
                    val prev = bladeTrail[i - 1]
                    val curr = bladeTrail[i]
                    val ratio = i.toFloat() / bladeTrail.size
                    val strokeWidth = (22f * ratio).coerceAtLeast(3f)
                    val alpha = (curr.alpha * ratio).coerceIn(0.05f, 1f)

                    drawLine(
                        color = Color(0xFF80DEEA).copy(alpha = alpha),
                        start = prev.offset,
                        end = curr.offset,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Partículas
            for (p in particles) {
                drawCircle(
                    color = p.color,
                    radius = p.radius * (1f - 0.5f * p.progress),
                    center = Offset(p.x, p.y),
                    alpha = 1f - p.progress
                )
            }

            // 4. Textos flotantes
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
// Pantalla principal, HUD y overlays
// ---------------------------------------------------------------------------

@Composable
fun SlashGameScreen(
    soundResMap: Map<SlashSoundEvent, Int> = emptyMap(),
    initialLives: Int = 3,
    initialDifficulty: SlashDifficulty = SlashDifficulty.NORMAL,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()
    val lifecycleOwner = LocalLifecycleOwner.current
    val soundPlayer = rememberSlashSoundPlayer(soundResMap = soundResMap)

    val controller = remember(initialLives, initialDifficulty) {
        SlashGameController(
            initialLives = initialLives,
            initialDifficulty = initialDifficulty,
            startOnDifficultySelect = true,
            onSoundEvent = { event, pitch ->
                soundPlayer.play(event = event, pitchRate = pitch)
            }
        )
    }

    //val handleBack = controller.phase == SlashGamePhase.PLAYING || controller.phase == SlashGamePhase.PAUSED
    //BackHandler(enabled = handleBack) {
    //    controller.togglePause()
    //}

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
            .background(Color(0xFF14101A))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(controller) {
                    detectDragGestures(
                        onDragStart = { start -> controller.onDragStart(start) },
                        onDrag = { change, _ ->
                            change.consume()
                            if (controller.onDragMove(change.position)) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                        onDragEnd = { controller.onDragEnd() },
                        onDragCancel = { controller.onDragEnd() }
                    )
                }
        ) {
            controller.updateBoardSize(size.width, size.height)
            controller.renderScene(this, textMeasurer)
        }

        if (controller.phase != SlashGamePhase.DIFFICULTY_SELECT) {
            SlashHud(
                score = controller.score,
                combo = controller.combo,
                level = controller.level,
                difficultyLabel = controller.difficulty.label,
                lives = controller.lives,
                maxLives = controller.initialLives,
                isPaused = controller.phase == SlashGamePhase.PAUSED,
                onTogglePause = { controller.togglePause() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(12.dp)
            )
        }

        when (controller.phase) {
            SlashGamePhase.DIFFICULTY_SELECT -> {
                SlashDifficultyOverlay(
                    selectedDifficulty = controller.difficulty,
                    bestScore = controller.bestScore,
                    onSelectDifficulty = { controller.selectDifficulty(it) },
                    onStartGame = { controller.startGame(controller.difficulty) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            SlashGamePhase.PAUSED -> {
                SlashPauseOverlay(
                    difficultyLabel = controller.difficulty.label,
                    score = controller.score,
                    onResume = { controller.resumeGame() },
                    onRestart = { controller.startGame(controller.difficulty) },
                    onChangeDifficulty = { controller.openDifficultySelector() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            SlashGamePhase.GAME_OVER -> {
                SlashGameOverOverlay(
                    score = controller.score,
                    bestScore = controller.bestScore,
                    levelReached = controller.level,
                    difficultyLabel = controller.difficulty.label,
                    onRestart = { controller.startGame(controller.difficulty) },
                    onChangeDifficulty = { controller.openDifficultySelector() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            SlashGamePhase.PLAYING -> Unit
        }
    }
}

@Composable
private fun SlashHud(
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
        color = Color(0xCC211A2B)
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
                    color = Color(0xFF80DEEA)
                )
                if (combo > 1) {
                    Text(
                        text = "RACHA x$combo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
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
                                    color = if (active) Color(0xFFEF5350) else Color(0xFF3E354B),
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
private fun SlashDifficultyOverlay(
    selectedDifficulty: SlashDifficulty,
    bestScore: Int,
    onSelectDifficulty: (SlashDifficulty) -> Unit,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xF21D1726),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "SWIPE & SLASH",
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
                SlashDifficulty.entries.forEach { option ->
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
                text = slashDifficultySummary(selectedDifficulty),
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

private fun slashDifficultySummary(difficulty: SlashDifficulty): String = when (difficulty) {
    SlashDifficulty.EASY -> "Impulsos más controlados, más tiempo entre oleadas y puntos base x0.8."
    SlashDifficulty.NORMAL -> "Parábolas equilibradas, objetivos blindados, bombas y puntos base x1.0."
    SlashDifficulty.HARD -> "Trayectorias rápidas, mayor densidad en pantalla y puntos base x1.35."
}

@Composable
private fun SlashPauseOverlay(
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
        color = Color(0xF21D1726),
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
                color = Color(0xFF80DEEA)
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
private fun SlashGameOverOverlay(
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
        color = Color(0xF21D1726),
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
