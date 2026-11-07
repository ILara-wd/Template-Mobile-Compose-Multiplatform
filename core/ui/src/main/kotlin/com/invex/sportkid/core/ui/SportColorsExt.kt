package com.invex.sportkid.core.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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

/** Icono vectorial del deporte (tintable). */
@get:DrawableRes
val Sport.iconRes: Int
    get() = when (this) {
        Sport.TAEKWONDO -> R.drawable.ic_sport_tkd
        Sport.SWIMMING -> R.drawable.ic_sport_swimming
        Sport.ATHLETICS -> R.drawable.ic_sport_athletics
    }

/** Nombre visible del deporte. */
@get:StringRes
val Sport.labelRes: Int
    get() = when (this) {
        Sport.TAEKWONDO -> R.string.sport_taekwondo
        Sport.SWIMMING -> R.string.sport_swimming
        Sport.ATHLETICS -> R.string.sport_athletics
    }
