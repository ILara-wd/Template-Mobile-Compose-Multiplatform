package com.akl.templatemobile.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Clase sellada que define las rutas de navegación de la aplicación.
 *
 * Cada subclase representa una pantalla o destino específico en la navegación.
 * Utiliza serialización para facilitar el paso de argumentos entre destinos.
 */
sealed class Routes : NavKey {
    @Serializable
    data object Home : Routes()

    @Serializable
    data class Detail(val id: Int) : Routes()

    @Serializable
    data object Side : Routes()

    @Serializable
    data object Error : Routes()

}