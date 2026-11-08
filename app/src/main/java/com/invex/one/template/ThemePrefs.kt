package com.invex.one.template

import android.content.Context
import com.invex.sportkid.core.designsystem.theme.AppPalette
import com.invex.sportkid.core.designsystem.theme.ThemeMode

/**
 * Tema elegido en P21, guardado en el dispositivo. "Automático" en Android 11 o menor cae a
 * Océano. TODO: DataStore y sincronizarlo con la nube familiar ("en todos tus dispositivos").
 */
internal class ThemePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("sk_theme", Context.MODE_PRIVATE)

    val palette: AppPalette
        get() = AppPalette.entries.firstOrNull { it.name == prefs.getString(KEY_PALETTE, null) }
            ?.takeIf { it in AppPalette.available } ?: AppPalette.Ocean

    val mode: ThemeMode
        get() = ThemeMode.entries.firstOrNull { it.name == prefs.getString(KEY_MODE, null) } ?: ThemeMode.System

    fun save(palette: AppPalette, mode: ThemeMode) {
        prefs.edit().putString(KEY_PALETTE, palette.name).putString(KEY_MODE, mode.name).apply()
    }

    private companion object {
        const val KEY_PALETTE = "palette"
        const val KEY_MODE = "mode"
    }
}
