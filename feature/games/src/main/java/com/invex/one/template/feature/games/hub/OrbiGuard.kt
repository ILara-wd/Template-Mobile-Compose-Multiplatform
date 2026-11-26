package com.invex.one.template.feature.games.hub

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
//import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import java.lang.Math.abs
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Modelos y configuración de dificultad
// ---------------------------------------------------------------------------

enum class OrbitGamePhase {
    DIFFICULTY_SELECT,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class OrbitDifficulty(
    val label: String,
    val baseSpawnInterval: Float,
    val spawnDecayPerLevel: Float,
    val baseSpeedMultiplier: Float,
    val speedStepPerLevel: Float,
    val shieldArcDeg: Float,
    val baseAngularSpeedRad: Float,
    val maxActiveBase: Int,
    val pointsPerLevel: Int,
    val scoreMultiplier: Float
) {
    EASY(
        label = "Fácil",
        baseSpawnInterval = 1.20f,
        spawnDecayPerLevel = 0.06f,
        baseSpeedMultiplier = 0.82f,
        speedStepPerLevel = 0.10f,
        shieldArcDeg = 92f,
        baseAngularSpeedRad = 2.4f,
        maxActiveBase = 5,
        pointsPerLevel = 500,
        scoreMultiplier = 0.8f
    ),
    NORMAL(
        label = "Normal",
        baseSpawnInterval = 0.92f,
        spawnDecayPerLevel = 0.07f,
        baseSpeedMultiplier = 1.0f,
        speedStepPerLevel = 0.14f,
        shieldArcDeg = 76f,
        baseAngularSpeedRad = 3.0f,
        maxActiveBase = 7,
        pointsPerLevel = 500,
        scoreMultiplier = 1.0f
    ),
    HARD(
        label = "Difícil",
        baseSpawnInterval = 0.68f,
        spawnDecayPerLevel = 0.08f,
        baseSpeedMultiplier = 1.28f,
        speedStepPerLevel = 0.17f,
        shieldArcDeg = 62f,
        baseAngularSpeedRad = 3.6f,
        maxActiveBase = 10,
        pointsPerLevel = 650,
        scoreMultiplier = 1.35f
    )
}

enum class OrbitSoundEvent {
    FLIP_SHIELD,
    BLOCK_THREAT,
    ORB_SCORED,
    CORE_HIT,
    GAME_OVER
}

enum class OrbitEntityKind {
    THREAT,       // Bloquear con el escudo; daña el núcleo si entra
    HEAVY_THREAT, // Requiere 2 bloqueos para romperse
    CORE_ORB      // Dejar pasar al núcleo para ganar puntos extra
}

data class OrbitEntity(
    val id: Long,
    val kind: OrbitEntityKind,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    var hp: Int,
    var bounceCooldown: Float = 0f
)

data class OrbitParticle(
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

data class OrbitPopup(
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

data class OrbitLevelConfig(
    val level: Int,
    val spawnIntervalSeconds: Float,
    val speedMultiplier: Float,
    val angularSpeedRad: Float,
    val maxActiveEntities: Int
)

fun configForOrbitLevel(difficulty: OrbitDifficulty, level: Int): OrbitLevelConfig {
    val clampedLevel = max(1, level)
    val levelOffset = clampedLevel - 1
    return OrbitLevelConfig(
        level = clampedLevel,
        spawnIntervalSeconds = max(
            0.22f,
            difficulty.baseSpawnInterval - levelOffset * difficulty.spawnDecayPerLevel
        ),
        speedMultiplier = difficulty.baseSpeedMultiplier + levelOffset * difficulty.speedStepPerLevel,
        angularSpeedRad = (difficulty.baseAngularSpeedRad + levelOffset * 0.22f).coerceAtMost(6.2f),
        maxActiveEntities = (difficulty.maxActiveBase + levelOffset * 2).coerceAtMost(28)
    )
}

// ---------------------------------------------------------------------------
// Reproductor de sonido con SoundPool (baja latencia en Android)
// ---------------------------------------------------------------------------

class OrbitSoundPoolPlayer(
    context: Context,
    private val soundResMap: Map<OrbitSoundEvent, Int>
) {
    private val soundPool: SoundPool
    private val loadedIds = mutableMapOf<OrbitSoundEvent, Int>()
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

    fun play(event: OrbitSoundEvent, pitchRate: Float = 1.0f) {
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
fun rememberOrbitSoundPlayer(
    soundResMap: Map<OrbitSoundEvent, Int>
): OrbitSoundPoolPlayer {
    val context = LocalContext.current.applicationContext
    val player = remember(soundResMap) {
        OrbitSoundPoolPlayer(context, soundResMap)
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    return player
}

// ---------------------------------------------------------------------------
// Controlador principal de Orbit Guard
// ---------------------------------------------------------------------------

class OrbitGuardController(
    val initialLives: Int = 3,
    initialDifficulty: OrbitDifficulty = OrbitDifficulty.NORMAL,
    startOnDifficultySelect: Boolean = true,
    private val onSoundEvent: ((OrbitSoundEvent, Float) -> Unit)? = null
) {
    var difficulty by mutableStateOf(initialDifficulty)
        private set
    var phase by mutableStateOf(
        if (startOnDifficultySelect) OrbitGamePhase.DIFFICULTY_SELECT else OrbitGamePhase.PLAYING
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
    private var centerX = 0f
    private var centerY = 0f

    private val coreRadius = 34f
    private val orbitRadius = 112f
    private var shieldAngleRad = -PI.toFloat() / 2f
    private var orbitDirection = 1f // 1f = horario, -1f = antihorario
    private var flipFlashTimer = 0f

    private val entities = mutableListOf<OrbitEntity>()
    private val particles = mutableListOf<OrbitParticle>()
    private val popups = mutableListOf<OrbitPopup>()

    private var nextId = 1L
    private var spawnAccumulator = 0f

    fun updateBoardSize(width: Float, height: Float) {
        boardWidth = width
        boardHeight = height
        centerX = width / 2f
        centerY = height / 2f
    }

    fun selectDifficulty(selected: OrbitDifficulty) {
        difficulty = selected
    }

    fun startGame(selectedDifficulty: OrbitDifficulty = difficulty) {
        difficulty = selectedDifficulty
        entities.clear()
        particles.clear()
        popups.clear()

        score = 0
        combo = 0
        level = 1
        lives = initialLives
        shieldAngleRad = -PI.toFloat() / 2f
        orbitDirection = 1f
        flipFlashTimer = 0f
        spawnAccumulator = 0f

        phase = OrbitGamePhase.PLAYING
        frameTick++
    }

    fun openDifficultySelector() {
        phase = OrbitGamePhase.DIFFICULTY_SELECT
        frameTick++
    }

    fun pauseGame() {
        if (phase == OrbitGamePhase.PLAYING) {
            phase = OrbitGamePhase.PAUSED
            frameTick++
        }
    }

    fun resumeGame() {
        if (phase == OrbitGamePhase.PAUSED) {
            phase = OrbitGamePhase.PLAYING
            frameTick++
        }
    }

    fun togglePause() {
        when (phase) {
            OrbitGamePhase.PLAYING -> pauseGame()
            OrbitGamePhase.PAUSED -> resumeGame()
            else -> Unit
        }
    }

    /**
     * Invierte el sentido de giro del escudo al tocar la pantalla.
     */
    fun onBoardTap(): Boolean {
        if (phase != OrbitGamePhase.PLAYING) return false
        orbitDirection = -orbitDirection
        flipFlashTimer = 0.14f
        onSoundEvent?.invoke(OrbitSoundEvent.FLIP_SHIELD, if (orbitDirection > 0f) 1.05f else 0.95f)
        frameTick++
        return true
    }

    fun step(deltaSeconds: Float) {
        if (boardWidth <= 0f || boardHeight <= 0f) return
        if (phase == OrbitGamePhase.DIFFICULTY_SELECT || phase == OrbitGamePhase.PAUSED) {
            return
        }

        updateParticlesAndPopups(deltaSeconds)

        if (phase != OrbitGamePhase.PLAYING) {
            frameTick++
            return
        }

        if (flipFlashTimer > 0f) {
            flipFlashTimer = max(0f, flipFlashTimer - deltaSeconds)
        }

        level = 1 + (score / difficulty.pointsPerLevel)
        val config = configForOrbitLevel(difficulty, level)

        // Rotación del escudo orbital
        shieldAngleRad = normalizeAngleRad(
            shieldAngleRad + orbitDirection * config.angularSpeedRad * deltaSeconds
        )

        spawnAccumulator += deltaSeconds
        if (spawnAccumulator >= config.spawnIntervalSeconds && entities.size < config.maxActiveEntities) {
            spawnAccumulator = 0f
            spawnEntity(config.speedMultiplier)
        }

        val iterator = entities.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            if (entity.bounceCooldown > 0f) {
                entity.bounceCooldown = max(0f, entity.bounceCooldown - deltaSeconds)
            }

            entity.x += entity.vx * deltaSeconds
            entity.y += entity.vy * deltaSeconds

            val dx = entity.x - centerX
            val dy = entity.y - centerY
            val distToCenter = hypot(dx, dy)
            val entityAngleRad = atan2(dy, dx)

            // Colisión con el arco del escudo orbital
            if (entity.bounceCooldown <= 0f && isOnShieldArc(distToCenter, entity.radius, entityAngleRad)) {
                when (entity.kind) {
                    OrbitEntityKind.THREAT, OrbitEntityKind.HEAVY_THREAT -> {
                        entity.hp -= 1
                        if (entity.hp <= 0) {
                            combo += 1
                            val multiplier = 1 + (combo / 5)
                            val basePts = if (entity.kind == OrbitEntityKind.HEAVY_THREAT) 180 else 100
                            val earned = (basePts * multiplier * difficulty.scoreMultiplier)
                                .toInt()
                                .coerceAtLeast(25)
                            addScore(earned)

                            val pitch = (0.95f + (combo % 10) * 0.035f).coerceAtMost(1.4f)
                            onSoundEvent?.invoke(OrbitSoundEvent.BLOCK_THREAT, pitch)
                            spawnBurst(
                                x = entity.x,
                                y = entity.y,
                                color = if (entity.kind == OrbitEntityKind.HEAVY_THREAT) Color(0xFFCE93D8) else Color(0xFFFF8A65),
                                count = 13,
                                maxSpeed = 300f
                            )
                            popups.add(
                                OrbitPopup(
                                    text = "+$earned",
                                    x = entity.x,
                                    y = entity.y - 12f,
                                    vy = -105f,
                                    color = Color(0xFFFFF59D)
                                )
                            )
                            iterator.remove()
                            continue
                        } else {
                            // Primer golpe a amenaza pesada: rebota hacia afuera
                            entity.bounceCooldown = 0.25f
                            val normalX = if (distToCenter > 0.001f) dx / distToCenter else 0f
                            val normalY = if (distToCenter > 0.001f) dy / distToCenter else -1f
                            val speed = hypot(entity.vx, entity.vy)
                            entity.vx = normalX * speed * 0.85f
                            entity.vy = normalY * speed * 0.85f
                            onSoundEvent?.invoke(OrbitSoundEvent.BLOCK_THREAT, 0.85f)
                            spawnBurst(x = entity.x, y = entity.y, color = Color.White, count = 6, maxSpeed = 190f)
                            popups.add(
                                OrbitPopup(
                                    text = "1 MÁS!",
                                    x = entity.x,
                                    y = entity.y - 10f,
                                    vy = -85f,
                                    color = Color(0xFFCE93D8),
                                    fontSizeSp = 15f,
                                    maxAge = 0.55f
                                )
                            )
                        }
                    }

                    OrbitEntityKind.CORE_ORB -> {
                        // El escudo desvía el orbe hacia afuera sin romper el combo
                        entity.bounceCooldown = 0.25f
                        val normalX = if (distToCenter > 0.001f) dx / distToCenter else 0f
                        val normalY = if (distToCenter > 0.001f) dy / distToCenter else -1f
                        val speed = hypot(entity.vx, entity.vy)
                        entity.vx = normalX * speed * 0.9f
                        entity.vy = normalY * speed * 0.9f
                        onSoundEvent?.invoke(OrbitSoundEvent.FLIP_SHIELD, 1.2f)
                        spawnBurst(x = entity.x, y = entity.y, color = Color(0xFF4DD0E1), count = 6, maxSpeed = 160f)
                    }
                }
            }

            // Llegada al núcleo central
            if (distToCenter <= coreRadius + entity.radius * 0.75f) {
                when (entity.kind) {
                    OrbitEntityKind.CORE_ORB -> {
                        combo += 1
                        val multiplier = 1 + (combo / 5)
                        val earned = (150 * multiplier * difficulty.scoreMultiplier)
                            .toInt()
                            .coerceAtLeast(35)
                        addScore(earned)
                        onSoundEvent?.invoke(OrbitSoundEvent.ORB_SCORED, 1.2f)
                        spawnBurst(x = centerX, y = centerY, color = Color(0xFF4DD0E1), count = 14, maxSpeed = 260f)
                        popups.add(
                            OrbitPopup(
                                text = "+$earned",
                                x = centerX,
                                y = centerY - coreRadius - 12f,
                                vy = -110f,
                                color = Color(0xFF80DEEA)
                            )
                        )
                        iterator.remove()
                    }

                    OrbitEntityKind.THREAT, OrbitEntityKind.HEAVY_THREAT -> {
                        combo = 0
                        lives = max(0, lives - 1)
                        spawnBurst(x = centerX, y = centerY, color = Color(0xFFEF5350), count = 16, maxSpeed = 320f)
                        popups.add(
                            OrbitPopup(
                                text = "-1 VIDA",
                                x = centerX,
                                y = centerY - coreRadius - 12f,
                                vy = -100f,
                                color = Color(0xFFEF5350)
                            )
                        )
                        iterator.remove()

                        if (lives == 0) {
                            phase = OrbitGamePhase.GAME_OVER
                            if (score > bestScore) bestScore = score
                            onSoundEvent?.invoke(OrbitSoundEvent.GAME_OVER, 1.0f)
                            break
                        } else {
                            onSoundEvent?.invoke(OrbitSoundEvent.CORE_HIT, 0.9f)
                        }
                    }
                }
                continue
            }

            // Limpiar entidades que salieron muy lejos tras rebotar
            val maxReach = hypot(boardWidth / 2f, boardHeight / 2f) + entity.radius * 3f
            if (distToCenter > maxReach) {
                iterator.remove()
            }
        }

        frameTick++
    }

    private fun isOnShieldArc(distToCenter: Float, entityRadius: Float, entityAngleRad: Float): Boolean {
        val radialTolerance = entityRadius + 12f
        if (abs(distToCenter - orbitRadius) > radialTolerance) return false

        val halfArcRad = Math.toRadians((difficulty.shieldArcDeg / 2.0)).toFloat() + 0.08f
        val diff = abs(normalizeAngleRad(entityAngleRad - shieldAngleRad))
        return diff <= halfArcRad
    }

    private fun normalizeAngleRad(angle: Float): Float {
        var a = angle % (2.0 * PI).toFloat()
        if (a > PI) a -= (2.0 * PI).toFloat()
        if (a < -PI) a += (2.0 * PI).toFloat()
        return a
    }

    private fun addScore(points: Int) {
        score += points
        if (score > bestScore) {
            bestScore = score
        }
    }

    private fun spawnEntity(speedMultiplier: Float) {
        val roll = Random.nextFloat()
        val kind = when {
            roll < 0.24f -> OrbitEntityKind.CORE_ORB
            roll < 0.80f -> OrbitEntityKind.THREAT
            else -> OrbitEntityKind.HEAVY_THREAT
        }

        val radius = when (kind) {
            OrbitEntityKind.HEAVY_THREAT -> 24f
            OrbitEntityKind.CORE_ORB -> 17f
            OrbitEntityKind.THREAT -> 20f
        }

        val spawnRadius = max(boardWidth, boardHeight) * 0.58f + radius
        val angleRad = Random.nextFloat() * (2.0 * PI).toFloat()
        val startX = centerX + cos(angleRad) * spawnRadius
        val startY = centerY + sin(angleRad) * spawnRadius

        // Ligera desviación angular para que no todos vayan en línea recta exacta al centro
        val aimJitterRad = (Random.nextFloat() * 0.24f - 0.12f)
        val travelAngleRad = angleRad + PI.toFloat() + aimJitterRad

        val speed = when (kind) {
            OrbitEntityKind.HEAVY_THREAT -> Random.nextFloat() * 55f + 135f
            OrbitEntityKind.CORE_ORB -> Random.nextFloat() * 50f + 150f
            OrbitEntityKind.THREAT -> Random.nextFloat() * 65f + 165f
        } * speedMultiplier

        val hp = if (kind == OrbitEntityKind.HEAVY_THREAT) 2 else 1

        entities.add(
            OrbitEntity(
                id = nextId++,
                kind = kind,
                x = startX,
                y = startY,
                vx = cos(travelAngleRad) * speed,
                vy = sin(travelAngleRad) * speed,
                radius = radius,
                hp = hp
            )
        )
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

    private fun spawnBurst(x: Float, y: Float, color: Color, count: Int, maxSpeed: Float) {
        if (particles.size >= 260) return
        val allowed = minOf(count, 260 - particles.size)
        for (i in 0 until allowed) {
            val angle = (2.0 * PI * i / allowed) + (Random.nextFloat() * 0.45 - 0.225)
            val speed = Random.nextFloat() * maxSpeed * 0.6f + maxSpeed * 0.4f
            particles.add(
                OrbitParticle(
                    x = x,
                    y = y,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    radius = Random.nextFloat() * 5.5f + 3.5f,
                    color = color,
                    maxAge = Random.nextFloat() * 0.2f + 0.35f
                )
            )
        }
    }

    fun renderScene(drawScope: DrawScope, textMeasurer: TextMeasurer) {
        val unused = frameTick
        with(drawScope) {
            if (boardWidth <= 0f || boardHeight <= 0f) return

            // 1. Pista orbital de referencia
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = orbitRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.5f)
            )

            // 2. Núcleo central
            drawCircle(
                color = Color(0xFF1E88E5),
                radius = coreRadius,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xFF90CAF9),
                radius = coreRadius * 0.55f,
                center = Offset(centerX, centerY)
            )

            // 3. Escudo orbital
            val shieldStartDeg = Math.toDegrees((shieldAngleRad - Math.toRadians((difficulty.shieldArcDeg / 2.0))).toDouble()).toFloat()
            val shieldColor = if (flipFlashTimer > 0f) Color(0xFFFFF59D) else Color(0xFF4DD0E1)
            drawArc(
                color = shieldColor,
                startAngle = shieldStartDeg,
                sweepAngle = difficulty.shieldArcDeg,
                useCenter = false,
                topLeft = Offset(centerX - orbitRadius, centerY - orbitRadius),
                size = Size(orbitRadius * 2f, orbitRadius * 2f),
                style = Stroke(width = 14f, cap = StrokeCap.Round)
            )

            // 4. Entidades entrantes
            for (entity in entities) {
                val color = when (entity.kind) {
                    OrbitEntityKind.THREAT -> Color(0xFFEF5350)
                    OrbitEntityKind.HEAVY_THREAT -> Color(0xFFAB47BC)
                    OrbitEntityKind.CORE_ORB -> Color(0xFF26C6DA)
                }
                drawCircle(
                    color = color,
                    radius = entity.radius,
                    center = Offset(entity.x, entity.y)
                )
            }

            // 5. Partículas
            for (p in particles) {
                drawCircle(
                    color = p.color,
                    radius = p.radius * (1f - 0.5f * p.progress),
                    center = Offset(p.x, p.y),
                    alpha = 1f - p.progress
                )
            }

            // 6. Textos flotantes
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
fun OrbitGuardScreen(
    soundResMap: Map<OrbitSoundEvent, Int> = emptyMap(),
    initialLives: Int = 3,
    initialDifficulty: OrbitDifficulty = OrbitDifficulty.NORMAL,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()
    val lifecycleOwner = LocalLifecycleOwner.current
    val soundPlayer = rememberOrbitSoundPlayer(soundResMap = soundResMap)

    val controller = remember(initialLives, initialDifficulty) {
        OrbitGuardController(
            initialLives = initialLives,
            initialDifficulty = initialDifficulty,
            startOnDifficultySelect = true,
            onSoundEvent = { event, pitch ->
                soundPlayer.play(event = event, pitchRate = pitch)
            }
        )
    }

    //val handleBack = controller.phase == OrbitGamePhase.PLAYING || controller.phase == OrbitGamePhase.PAUSED
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
            .background(Color(0xFF0E131A))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(controller) {
                    detectTapGestures {
                        if (controller.onBoardTap()) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                }
        ) {
            controller.updateBoardSize(size.width, size.height)
            controller.renderScene(this, textMeasurer)
        }

        if (controller.phase != OrbitGamePhase.DIFFICULTY_SELECT) {
            OrbitHud(
                score = controller.score,
                combo = controller.combo,
                level = controller.level,
                difficultyLabel = controller.difficulty.label,
                lives = controller.lives,
                maxLives = controller.initialLives,
                isPaused = controller.phase == OrbitGamePhase.PAUSED,
                onTogglePause = { controller.togglePause() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(12.dp)
            )
        }

        when (controller.phase) {
            OrbitGamePhase.DIFFICULTY_SELECT -> {
                OrbitDifficultyOverlay(
                    selectedDifficulty = controller.difficulty,
                    bestScore = controller.bestScore,
                    onSelectDifficulty = { controller.selectDifficulty(it) },
                    onStartGame = { controller.startGame(controller.difficulty) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            OrbitGamePhase.PAUSED -> {
                OrbitPauseOverlay(
                    difficultyLabel = controller.difficulty.label,
                    score = controller.score,
                    onResume = { controller.resumeGame() },
                    onRestart = { controller.startGame(controller.difficulty) },
                    onChangeDifficulty = { controller.openDifficultySelector() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            OrbitGamePhase.GAME_OVER -> {
                OrbitGameOverOverlay(
                    score = controller.score,
                    bestScore = controller.bestScore,
                    levelReached = controller.level,
                    difficultyLabel = controller.difficulty.label,
                    onRestart = { controller.startGame(controller.difficulty) },
                    onChangeDifficulty = { controller.openDifficultySelector() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            OrbitGamePhase.PLAYING -> Unit
        }
    }
}

@Composable
private fun OrbitHud(
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
        color = Color(0xCC18202C)
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
                if (combo > 1) {
                    Text(
                        text = "COMBO x$combo",
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
                                    color = if (active) Color(0xFFEF5350) else Color(0xFF334155),
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
private fun OrbitDifficultyOverlay(
    selectedDifficulty: OrbitDifficulty,
    bestScore: Int,
    onSelectDifficulty: (OrbitDifficulty) -> Unit,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xF2151C26),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "ORBIT GUARD",
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
                OrbitDifficulty.entries.forEach { option ->
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
                text = orbitDifficultySummary(selectedDifficulty),
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

private fun orbitDifficultySummary(difficulty: OrbitDifficulty): String = when (difficulty) {
    OrbitDifficulty.EASY -> "Escudo más ancho, giro moderado y puntos base x0.8."
    OrbitDifficulty.NORMAL -> "Toca para invertir giro: bloquea amenazas y deja pasar orbes."
    OrbitDifficulty.HARD -> "Arco más estrecho, giro veloz, más amenazas y puntos base x1.35."
}

@Composable
private fun OrbitPauseOverlay(
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
        color = Color(0xF2151C26),
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
private fun OrbitGameOverOverlay(
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
        color = Color(0xF2151C26),
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