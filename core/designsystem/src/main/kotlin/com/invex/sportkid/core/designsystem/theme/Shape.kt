package com.invex.sportkid.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Formas (README §1.4). Cards = `large`, bottom sheets y diálogos = `extraLarge`. */
val SportKidShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp), // checkbox, mini‑badges
    small = RoundedCornerShape(8.dp), // badges de estado, tiles pequeños
    medium = RoundedCornerShape(12.dp), // inputs, botones tonales cuadrados, celdas
    large = RoundedCornerShape(20.dp), // cards de acción (Figma: Bienvenida)
    extraLarge = RoundedCornerShape(28.dp), // bottom sheets, diálogos
)

/** Pill: botones principales, chips, FAB extendido. */
@Suppress("UnusedReceiverParameter")
val Shapes.full: Shape get() = CircleShape

private val HeroShape = RoundedCornerShape(24.dp)

/** Card con ilustración hero (Login). */
@Suppress("UnusedReceiverParameter")
val Shapes.hero: Shape get() = HeroShape
