package com.akl.templatemobile.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import com.akl.hashshafiles.components.StyledText

/**
 * Componente de botón primario reutilizable.
 *
 * Muestra un botón con el texto proporcionado y ejecuta la acción indicada al hacer clic.
 *
 * @param text Texto que se mostrará en el botón.
 * @param onClick Acción a ejecutar cuando se presiona el botón.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit
) {
    Row {
        Button(onClick = { onClick() }) { StyledText(text = text) }
    }
}
