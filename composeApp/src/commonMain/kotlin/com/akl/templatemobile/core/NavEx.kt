package com.akl.templatemobile.core

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Navega a una pantalla específica agregando la clave de navegación al stack.
 *
 * @param screen Clave de la pantalla a la que se desea navegar.
 */
fun NavBackStack<NavKey>.navigateTo(screen: NavKey) {
    add(screen)
}

/**
 * Retrocede una pantalla en la pila de navegación.
 *
 * Elimina la última clave de navegación del stack, regresando a la pantalla anterior.
 * Si la pila está vacía, no realiza ninguna acción.
 */
fun NavBackStack<NavKey>.back() {
    if (isEmpty()) return
    removeLastOrNull()
}

/**
 * Retrocede en la pila de navegación hasta la pantalla objetivo especificada.
 *
 * Elimina las pantallas del stack una por una hasta que la pantalla `targetScreen` sea la última en la pila.
 * Si la pantalla objetivo no se encuentra en la pila, no realiza ninguna acción.
 *
 * @param targetScreen Clave de la pantalla a la que se desea regresar.
 */
fun NavBackStack<NavKey>.backTo(targetScreen: NavKey) {
    if (isEmpty()) return
    if (targetScreen !in this) return

    while (isNotEmpty() && last() != targetScreen) {
        removeLastOrNull()
    }

}
