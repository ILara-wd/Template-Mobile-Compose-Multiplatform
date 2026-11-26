package com.invex.one.template.feature.games.hub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class Fighter(
    val x: Float,
    val y: Float,
    val vy: Float = 0f,
    val hp: Int = 100,
    val facingRight: Boolean = true,
    val attackTimer: Float = 0f,
    val hitCooldown: Float = 0f
) {
    val width = 70f
    val height = 140f

    fun hurtbox(): Rect = Rect(x, y, x + width, y + height)

    fun attackHitbox(): Rect? {
        if (attackTimer <= 0f) return null
        val reach = 75f
        val attackY = y + 25f
        val attackHeight = 35f
        return if (facingRight) {
            Rect(x + width, attackY, x + width + reach, attackY + attackHeight)
        } else {
            Rect(x - reach, attackY, x, attackY + attackHeight)
        }
    }
}

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
    knobSize: Dp = 50.dp,
    onMove: (xRatio: Float, yRatio: Float) -> Unit
) {
    val maxRadiusPx = with(LocalDensity.current) { (size - knobSize).toPx() / 2f }
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = knobOffset + dragAmount
                        val distance = hypot(newOffset.x, newOffset.y)

                        knobOffset = if (distance <= maxRadiusPx) {
                            newOffset
                        } else {
                            val angle = atan2(newOffset.y, newOffset.x)
                            Offset(
                                x = cos(angle) * maxRadiusPx,
                                y = sin(angle) * maxRadiusPx
                            )
                        }
                        onMove(
                            (knobOffset.x / maxRadiusPx).coerceIn(-1f, 1f),
                            (knobOffset.y / maxRadiusPx).coerceIn(-1f, 1f)
                        )
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Base del joystick
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = this.size.minDimension / 2f
            )
            // Palanca móvil (Knob)
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = knobSize.toPx() / 2f,
                center = center + knobOffset
            )
        }
    }
}

@Composable
fun ActionPadButton(
    label: String,
    color: Color,
    onPress: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(color.copy(alpha = 0.75f), shape = CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(onPress = { onPress() })
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun LandscapeFightingGame(
    modifier: Modifier = Modifier
) {
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    var player by remember { mutableStateOf(Fighter(x = 200f, y = 200f, facingRight = true)) }
    var enemy by remember { mutableStateOf(Fighter(x = 800f, y = 200f, facingRight = false)) }
    var joyX by remember { mutableFloatStateOf(0f) }
    var gameOverText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(gameOverText, canvasWidth, canvasHeight) {
        if (gameOverText != null || canvasWidth == 0f || canvasHeight == 0f) return@LaunchedEffect

        val groundFloor = canvasHeight - 40f
        val groundY = groundFloor - player.height
        val gravity = 2000f
        var lastTime = withFrameNanos { it }
        var aiTimer = 0f

        player = player.copy(x = canvasWidth * 0.2f, y = groundY)
        enemy = enemy.copy(x = canvasWidth * 0.75f, y = groundY)

        while (gameOverText == null) {
            val now = withFrameNanos { it }
            val dt = (now - lastTime) / 1_000_000_000f
            lastTime = now

            // 1. Movimiento fluido según inclinación del joystick (joyX entre -1f y 1f)
            val pX = (player.x + joyX * 420f * dt).coerceIn(20f, canvasWidth - player.width - 20f)
            var pVy = player.vy + gravity * dt
            var pY = player.y + pVy * dt
            if (pY >= groundY) {
                pY = groundY
                pVy = 0f
            }
            val pFacing = if (kotlin.math.abs(joyX) > 0.1f) joyX > 0 else (player.x < enemy.x)
            val pAttack = (player.attackTimer - dt).coerceAtLeast(0f)
            val pCooldown = (player.hitCooldown - dt).coerceAtLeast(0f)

            player = player.copy(
                x = pX, y = pY, vy = pVy,
                facingRight = pFacing,
                attackTimer = pAttack,
                hitCooldown = pCooldown
            )

            // 2. IA Enemigo
            aiTimer += dt
            val dist = player.x - enemy.x
            val eFacing = dist > 0
            var eX = enemy.x
            var eAttack = (enemy.attackTimer - dt).coerceAtLeast(0f)
            val eCooldown = (enemy.hitCooldown - dt).coerceAtLeast(0f)

            if (kotlin.math.abs(dist) > 120f) {
                eX += (if (eFacing) 1f else -1f) * 230f * dt
            } else if (aiTimer > 1.1f && eAttack == 0f) {
                eAttack = 0.25f
                aiTimer = 0f
            }

            enemy = enemy.copy(
                x = eX.coerceIn(20f, canvasWidth - enemy.width - 20f),
                y = groundY,
                facingRight = eFacing,
                attackTimer = eAttack,
                hitCooldown = eCooldown
            )

            // 3. Colisiones
            player.attackHitbox()?.let { hit ->
                if (hit.overlaps(enemy.hurtbox()) && enemy.hitCooldown == 0f) {
                    enemy = enemy.copy(hp = (enemy.hp - 15).coerceAtLeast(0), hitCooldown = 0.35f)
                }
            }
            enemy.attackHitbox()?.let { hit ->
                if (hit.overlaps(player.hurtbox()) && player.hitCooldown == 0f) {
                    player = player.copy(hp = (player.hp - 10).coerceAtLeast(0), hitCooldown = 0.35f)
                }
            }

            if (enemy.hp <= 0) gameOverText = "¡Ganaste!"
            if (player.hp <= 0) gameOverText = "Derrota"
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E2E))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            canvasWidth = size.width
            canvasHeight = size.height
            val groundFloor = size.height - 40f

            // Suelo
            drawRect(Color(0xFF313244), topLeft = Offset(0f, groundFloor), size = Size(size.width, 40f))

            // Jugador
            drawRect(
                color = if (player.hitCooldown > 0f) Color.White else Color(0xFF89B4FA),
                topLeft = Offset(player.x, player.y),
                size = Size(player.width, player.height)
            )
            player.attackHitbox()?.let { box ->
                drawRect(Color.Yellow, topLeft = box.topLeft, size = box.size)
            }

            // Enemigo
            drawRect(
                color = if (enemy.hitCooldown > 0f) Color.White else Color(0xFFF38BA8),
                topLeft = Offset(enemy.x, enemy.y),
                size = Size(enemy.width, enemy.height)
            )
            enemy.attackHitbox()?.let { box ->
                drawRect(Color(0xFFFF9E64), topLeft = box.topLeft, size = box.size)
            }
        }

        // HUD: Barras de vida arriba
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HealthBar("Jugador", player.hp, Color(0xFF89B4FA))
            HealthBar("CPU", enemy.hp, Color(0xFFF38BA8))
        }

        // Joystick virtual (Izquierda)
        VirtualJoystick(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 36.dp, bottom = 24.dp),
            onMove = { xRatio, yRatio ->
                joyX = xRatio
                // Opcional: saltar si empuja el joystick hacia arriba
                if (yRatio < -0.75f && player.vy == 0f) {
                    player = player.copy(vy = -780f)
                }
            }
        )

        // Botones de acción arcade (Derecha)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 36.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ActionPadButton(label = "SALTAR", color = Color(0xFF4CAF50)) {
                if (player.vy == 0f) player = player.copy(vy = -780f)
            }
            ActionPadButton(label = "GOLPE", color = Color(0xFFE53935)) {
                if (player.attackTimer == 0f) player = player.copy(attackTimer = 0.2f)
            }
        }
// Modal de fin de combate
        gameOverText?.let { result ->
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(0.85f), shape = MaterialTheme.shapes.medium)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(result, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val groundY = canvasHeight - 40f - player.height
                    player = Fighter(x = canvasWidth * 0.2f, y = groundY, facingRight = true)
                    enemy = Fighter(x = canvasWidth * 0.75f, y = groundY, facingRight = false)
                    joyX = 0f
                    gameOverText = null
                }) { Text("Revancha") }
            }
        }
    }
}

@Composable
fun HealthBar(name: String, hp: Int, color: Color) {
    Column {
        Text("$name ($hp%)", color = Color.White, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.width(200.dp).height(16.dp).background(Color.DarkGray)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(hp / 100f).background(color))
        }
    }
}