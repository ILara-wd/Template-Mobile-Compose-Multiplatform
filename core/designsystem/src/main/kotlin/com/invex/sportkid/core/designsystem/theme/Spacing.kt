package com.invex.sportkid.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Espaciado con base de 4dp (README §1.4). */
@Immutable
data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    /** Margen horizontal de pantalla. */
    val screenMargin: Dp = 16.dp,
    /** Padding interno de card. */
    val cardPadding: Dp = 16.dp,
    /** Separación entre cards. */
    val cardGap: Dp = 12.dp,
    /** Separación entre secciones. */
    val sectionGap: Dp = 24.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

/** Medidas fijas del sistema que no cambian con el tema. */
object SkDimens {
    /** Área táctil mínima. */
    val minTouchTarget: Dp = 48.dp

    /** Barra izquierda con el color del deporte en cards de evento e historial. */
    val sportAccentBar: Dp = 4.dp

    val iconSize: Dp = 24.dp
    val iconTile: Dp = 40.dp
    val iconTileSmall: Dp = 24.dp
}

/**
 * Elevación tonal (sin sombras pesadas), niveles M3.
 * Cards en `surfaceContainerLowest` sobre `surface`; nav y sheets en `surfaceContainer`.
 */
object SkElevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp

    val card: Dp = level1
    val navigation: Dp = level2
    val sheet: Dp = level2
    val fab: Dp = level3
}
