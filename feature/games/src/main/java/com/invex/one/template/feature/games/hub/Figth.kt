package com.invex.one.template.feature.games.hub

/*
data class Fighter(
    val x: Float,
    val y: Float,
    val vy: Float = 0f,
    val hp: Int = 100,
    val facingRight: Boolean = true,
    val attackTimer: Float = 0f, // > 0 significa que está golpeando
    val hitCooldown: Float = 0f  // Evita recibir daño múltiple por el mismo golpe
) {
    val width = 80f
    val height = 160f

    fun hurtbox(): Rect = Rect(x, y, x + width, y + height)

    fun attackHitbox(): Rect? {
        if (attackTimer <= 0f) return null
        val reach = 75f
        val attackY = y + 30f
        val attackHeight = 40f
        return if (facingRight) {
            Rect(x + width, attackY, x + width + reach, attackY + attackHeight)
        } else {
            Rect(x - reach, attackY, x, attackY + attackHeight)
        }
    }
}

@Composable
fun FightingGame(
    modifier: Modifier = Modifier
) {
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    var player by remember { mutableStateOf(Fighter(x = 150f, y = 400f, facingRight = true)) }
    var enemy by remember { mutableStateOf(Fighter(x = 600f, y = 400f, facingRight = false)) }
    var moveDir by remember { mutableFloatStateOf(0f) } // -1 izq, 0 quieto, 1 der
    var gameOverText by remember { mutableStateOf<String?>(null) }

    // Bucle principal (Game Loop)
    LaunchedEffect(gameOverText, canvasWidth, canvasHeight) {
        if (gameOverText != null || canvasWidth == 0f || canvasHeight == 0f) return@LaunchedEffect

        val groundY = canvasHeight - 220f
        val gravity = 1800f
        var lastTime = withFrameNanos { it }
        var aiTimer = 0f

        // Posicionar en el suelo al iniciar
        player = player.copy(y = groundY)
        enemy = enemy.copy(y = groundY)

        while (gameOverText == null) {
            val now = withFrameNanos { it }
            val dt = (now - lastTime) / 1_000_000_000f
            lastTime = now

            // 1. Actualizar Jugador
            var pX = (player.x + moveDir * 350f * dt).coerceIn(20f, canvasWidth - player.width - 20f)
            var pVy = player.vy + gravity * dt
            var pY = player.y + pVy * dt
            if (pY >= groundY) {
                pY = groundY
                pVy = 0f
            }
            val pFacing = if (moveDir != 0f) moveDir > 0 else (player.x < enemy.x)
            val pAttack = (player.attackTimer - dt).coerceAtLeast(0f)
            val pCooldown = (player.hitCooldown - dt).coerceAtLeast(0f)

            player = player.copy(
                x = pX, y = pY, vy = pVy,
                facingRight = pFacing,
                attackTimer = pAttack,
                hitCooldown = pCooldown
            )

            // 2. IA básica del Enemigo
            aiTimer += dt
            val dist = player.x - enemy.x
            val eFacing = dist > 0
            var eX = enemy.x
            var eAttack = (enemy.attackTimer - dt).coerceAtLeast(0f)
            val eCooldown = (enemy.hitCooldown - dt).coerceAtLeast(0f)

            if (kotlin.math.abs(dist) > 130f) {
                eX += (if (eFacing) 1f else -1f) * 200f * dt
            } else if (aiTimer > 1.2f && eAttack == 0f) {
                eAttack = 0.25f // Lanza golpe cada 1.2s si está cerca
                aiTimer = 0f
            }

            enemy = enemy.copy(
                x = eX.coerceIn(20f, canvasWidth - enemy.width - 20f),
                y = groundY,
                facingRight = eFacing,
                attackTimer = eAttack,
                hitCooldown = eCooldown
            )

            // 3. Detección de golpes (Hitbox vs Hurtbox)
            val playerHit = player.attackHitbox()
            if (playerHit != null && playerHit.overlaps(enemy.hurtbox()) && enemy.hitCooldown == 0f) {
                enemy = enemy.copy(hp = (enemy.hp - 15).coerceAtLeast(0), hitCooldown = 0.4f)
            }

            val enemyHit = enemy.attackHitbox()
            if (enemyHit != null && enemyHit.overlaps(player.hurtbox()) && player.hitCooldown == 0f) {
                player = player.copy(hp = (player.hp - 10).coerceAtLeast(0), hitCooldown = 0.4f)
            }

            // 4. Verificar victoria/derrota
            if (enemy.hp <= 0) gameOverText = "¡Ganaste!"
            if (player.hp <= 0) gameOverText = "Derrota"
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E2E))) {
        // Escenario y personajes
        Canvas(modifier = Modifier.fillMaxSize()) {
            canvasWidth = size.width
            canvasHeight = size.height
            val groundY = size.height - 60f

            // Suelo
            drawRect(Color(0xFF313244), topLeft = Offset(0f, groundY), size = Size(size.width, 60f))

            // Dibujar Jugador (Azul)
            drawRect(
                color = if (player.hitCooldown > 0f) Color.White else Color(0xFF89B4FA),
                topLeft = Offset(player.x, player.y),
                size = Size(player.width, player.height)
            )
            player.attackHitbox()?.let { box ->
                drawRect(Color.Yellow, topLeft = box.topLeft, size = box.size)
            }

            // Dibujar Enemigo (Rojo)
            drawRect(
                color = if (enemy.hitCooldown > 0f) Color.White else Color(0xFFF38BA8),
                topLeft = Offset(enemy.x, enemy.y),
                size = Size(enemy.width, enemy.height)
            )
            enemy.attackHitbox()?.let { box ->
                drawRect(Color(0xFFFF9E64), topLeft = box.topLeft, size = box.size)
            }
        }

        // Barras de vida superiores
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HealthBar("Jugador", player.hp, Color(0xFF89B4FA))
            HealthBar("CPU", enemy.hp, Color(0xFFF38BA8))
        }

        // Controles en pantalla
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { moveDir = -1f }) { Text("←") }
                Button(onClick = { moveDir = 0f }) { Text("Parar") }
                Button(onClick = { moveDir = 1f }) { Text("→") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (player.vy == 0f) player = player.copy(vy = -750f)
                }) { Text("Saltar") }
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    onClick = {
                        if (player.attackTimer == 0f) player = player.copy(attackTimer = 0.2f)
                    }
                ) { Text("Golpear") }
            }
        }

        // Pantalla de fin de combate
        gameOverText?.let { result ->
            Column(
                modifier = Modifier.align(Alignment.Center).background(Color.Black.copy(0.85f)).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(result, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    player = Fighter(x = 150f, y = canvasHeight - 220f, facingRight = true)
                    enemy = Fighter(x = 600f, y = canvasHeight - 220f, facingRight = false)
                    moveDir = 0f
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
        Box(modifier = Modifier.width(140.dp).height(16.dp).background(Color.DarkGray)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(hp / 100f).background(color))
        }
    }
}
*/