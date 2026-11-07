package com.invex.sportkid.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Sport

/** Par contenido / contenedor de un deporte (Icon Tile, acento lateral, tabs). */
@Immutable
data class SportColorRole(val content: Color, val container: Color)

/** Único punto donde un [Sport] del dominio se traduce a sus tokens de color. */
@Composable
@ReadOnlyComposable
fun Sport.colors(): SportColorRole {
    val c = SportKidTheme.sportColors
    return when (this) {
        Sport.TAEKWONDO -> SportColorRole(c.tkd, c.tkdContainer)
        Sport.SWIMMING -> SportColorRole(c.swimming, c.swimmingContainer)
        Sport.ATHLETICS -> SportColorRole(c.athletics, c.athleticsContainer)
    }
}
