package com.invex.sportkid.core.designsystem.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.ui.graphics.Color

/** Temas seleccionables en P21 (README §1.1). Cada uno tiene variante clara y oscura. */
enum class AppPalette(val seed: Color) {
    /** Material You (wallpaper). En Android 11 o menor cae a [Ocean]. */
    Automatic(OceanSeed),

    /** Default. Esquema estático exportado de Theme Builder. */
    Ocean(OceanSeed),

    /** Las siguientes se generan en runtime con MaterialKolor (Tonal Spot). */
    Dojang(Color(0xFF9C2A22)),
    Alberca(Color(0xFF006A6A)),
    Pista(Color(0xFFB45309)),
    Bosque(Color(0xFF386A20)),
    ;

    companion object {
        /** "Automático" se oculta en el ThemePicker cuando es `false`. */
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
        val isDynamicColorSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        /** Opciones a mostrar en el selector según el dispositivo. */
        val available: List<AppPalette>
            get() = if (isDynamicColorSupported) entries else entries - Automatic
    }
}

/** Modo claro/oscuro (botón segmentado "Sistema / Claro / Oscuro"). */
enum class ThemeMode { System, Light, Dark }
