package com.invex.sportkid.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta semántica fija (README §1.2). **No cambia con el tema**: deporte, medalla, estado,
 * color de cada hijo y cintas transmiten significado.
 *
 * `status.neutral` no es un color propio: se usa `onSurfaceVariant` + `outline` del ColorScheme.
 */
@Immutable
data class SportColors(
    val tkd: Color,
    val tkdContainer: Color,
    val swimming: Color,
    val swimmingContainer: Color,
    val athletics: Color,
    val athleticsContainer: Color,
    val medalGold: Color,
    val medalSilver: Color,
    val medalBronze: Color,
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val athlete: AthleteColors = AthleteColors(),
    val belt: BeltColors = BeltColors(),
)

/**
 * Colores para cada hijo (se eligen en P04 y se guardan en `Athlete.color`).
 *
 * Solo para el anillo del avatar, los puntos del calendario, el punto junto al nombre y el borde
 * del chip seleccionado. **Nunca** como fondo de texto: amarillo y naranja no cumplen 4.5:1.
 */
@Immutable
data class AthleteColors(
    val blue: Color = Color(0xFF1E88E5),
    val green: Color = Color(0xFF43A047),
    val purple: Color = Color(0xFF8E24AA),
    val pink: Color = Color(0xFFD81B60),
    val orange: Color = Color(0xFFFB8C00),
    val teal: Color = Color(0xFF00ACC1),
    val red: Color = Color(0xFFE53935),
    val yellow: Color = Color(0xFFFDD835),
) {
    /** Orden en que se ofrecen en el selector de color de P04. */
    val all: List<Color> get() = listOf(blue, green, purple, pink, orange, teal, red, yellow)
}

/**
 * Cintas de TKD por defecto (la secuencia es configurable por escuela).
 * - Blanca: dibujar con borde `outline`.
 * - Roja‑Negra: dividida en [red] / [black].
 * - Negra (Dan): [black] con franjas [danStripe] por cada Dan.
 */
@Immutable
data class BeltColors(
    val white: Color = Color(0xFFFFFFFF),
    val yellow: Color = Color(0xFFFBC02D),
    val orange: Color = Color(0xFFFB8C00),
    val green: Color = Color(0xFF2E7D32),
    val blue: Color = Color(0xFF1565C0),
    val brown: Color = Color(0xFF6D4C41),
    val red: Color = Color(0xFFC62828),
    val black: Color = Color(0xFF212121),
    val danStripe: Color = Color(0xFFD4A017),
)

internal val LightSportColors = SportColors(
    tkd = Color(0xFFC62828),
    tkdContainer = Color(0xFFFFEBEE),
    swimming = Color(0xFF00838F),
    swimmingContainer = Color(0xFFE0F7FA),
    athletics = Color(0xFFEF6C00),
    athleticsContainer = Color(0xFFFFF3E0),
    medalGold = Color(0xFFD4A017),
    medalSilver = Color(0xFF9E9E9E),
    medalBronze = Color(0xFFA0522D),
    success = Color(0xFF2E7D32),
    successContainer = Color(0xFFDCF5DD),
    warning = Color(0xFF8A5A00),
    warningContainer = Color(0xFFFFF3D6),
)

/** Variantes oscuras desaturadas. */
internal val DarkSportColors = SportColors(
    tkd = Color(0xFFFF8A80),
    tkdContainer = Color(0xFF4A1414),
    swimming = Color(0xFF4DD0E1),
    swimmingContainer = Color(0xFF0B3A3E),
    athletics = Color(0xFFFFB74D),
    athleticsContainer = Color(0xFF4A2A0B),
    medalGold = Color(0xFFE6C04A),
    medalSilver = Color(0xFFBDBDBD),
    medalBronze = Color(0xFFC98A5E),
    success = Color(0xFF81C784),
    successContainer = Color(0xFF1B3A1D),
    warning = Color(0xFFFFCC66),
    warningContainer = Color(0xFF3D2E0A),
)

val LocalSportColors = staticCompositionLocalOf { LightSportColors }
