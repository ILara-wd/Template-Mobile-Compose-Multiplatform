package com.akl.hashshafiles.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.akl.templatemobile.util.Constants.BOLD_STYLED_TEXT
import com.akl.templatemobile.util.Constants.ITALIC_STYLED_TEXT
import com.akl.templatemobile.util.Constants.LINE_THROUGH_STYLED_TEXT
import com.akl.templatemobile.util.Constants.SEMI_BOLD_STYLED_TEXT
import com.akl.templatemobile.util.Constants.UNDER_LINE_STYLED_TEXT
import com.akl.templatemobile.components.buildAnnotatedStringStyled

/**
 * Componente que muestra texto con estilos personalizados usando Compose.
 *
 * Permite aplicar negrita, itálica, subrayado y tachado a partes del texto
 * utilizando los siguientes delimitadores:
 * - °text° -> Bold
 * - #text# -> SemiBold
 * - ¬text¬ -> Italic
 * - _text_ -> Underline
 * - -text- -> LineThrough
 *
 * @param modifier Modificador para el componente.
 * @param textAlign Alineación del texto.
 * @param color Color del texto.
 * @param text Cadena de texto con los delimitadores de estilo.
 * @param style Estilo de texto adicional.
 */
@Composable
fun StyledText(
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Black,
    text: String,
    style: TextStyle = TextStyle.Default
) {
    Text(
        style = style,
        text = buildAnnotatedStringStyled(text),
        modifier = modifier,
        color = color,
        textAlign = textAlign
    )
}

/**
 * Extensión para aplicar estilo de seminegrita a un texto.
 *
 * Envuelve la cadena con el delimitador definido en `SEMI_BOLD_STYLED_TEXT`, permitiendo que
 * la función `buildAnnotatedStringStyled` lo detecte y aplique el estilo correspondiente.
 *
 * @return Cadena con delimitadores de seminegrita para ser procesada y estilizada.
 */
fun String.boldStyled(): String = "$SEMI_BOLD_STYLED_TEXT$this$SEMI_BOLD_STYLED_TEXT"

/**
 * Aplica el estilo de seminegrita al texto.
 *
 * Envuelve la cadena con el delimitador definido en `BOLD_STYLED_TEXT`, permitiendo que
 * la función `buildAnnotatedStringStyled` lo detecte y aplique el estilo de seminegrita.
 *
 * @return Cadena con delimitadores de seminegrita para ser procesada y estilizada.
 */
fun String.semiBoldStyled(): String = "$BOLD_STYLED_TEXT$this$BOLD_STYLED_TEXT"

/**
 * Aplica el estilo itálico al texto.
 *
 * Envuelve la cadena con el delimitador definido en `ITALIC_STYLED_TEXT`, permitiendo que
 * la función `buildAnnotatedStringStyled` lo detecte y aplique el estilo correspondiente.
 *
 * @return Cadena con delimitadores de itálica para ser procesada y estilizada.
 */
fun String.italicStyled(): String = "$ITALIC_STYLED_TEXT$this$ITALIC_STYLED_TEXT"

/**
 * Aplica el estilo de subrayado al texto.
 *
 * Envuelve la cadena con el delimitador definido en `UNDER_LINE_STYLED_TEXT`, permitiendo que
 * la función `buildAnnotatedStringStyled` lo detecte y aplique el estilo correspondiente.
 *
 * @return Cadena con delimitadores de subrayado para ser procesada y estilizada.
 */
fun String.underlineStyled(): String = "$UNDER_LINE_STYLED_TEXT$this$UNDER_LINE_STYLED_TEXT"

/**
 * Aplica el estilo de tachado al texto.
 *
 * Envuelve la cadena con el delimitador definido en `LINE_THROUGH_STYLED_TEXT`, permitiendo que
 * la función `buildAnnotatedStringStyled` lo detecte y aplique el estilo correspondiente.
 *
 * @return Cadena con delimitadores de tachado para ser procesada y estilizada.
 */
fun String.lineThroughStyled(): String = "$LINE_THROUGH_STYLED_TEXT$this$LINE_THROUGH_STYLED_TEXT"
