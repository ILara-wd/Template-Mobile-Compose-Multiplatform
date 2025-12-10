package com.akl.templatemobile.components

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.akl.templatemobile.util.Constants.BOLD_STYLED_TEXT
import com.akl.templatemobile.util.Constants.ITALIC_STYLED_TEXT
import com.akl.templatemobile.util.Constants.LINE_THROUGH_STYLED_TEXT
import com.akl.templatemobile.util.Constants.ONE
import com.akl.templatemobile.util.Constants.REGEX_STYLED_TEXT_PATTERN
import com.akl.templatemobile.util.Constants.SEMI_BOLD_STYLED_TEXT
import com.akl.templatemobile.util.Constants.TWO
import com.akl.templatemobile.util.Constants.UNDER_LINE_STYLED_TEXT
import com.akl.templatemobile.util.Constants.ZERO

/**
 * Construye una cadena anotada aplicando estilos personalizados a partes del texto.
 *
 * Busca patrones delimitados en el texto para aplicar seminegrita, negrita, itálica, subrayado o tachado,
 * según los delimitadores definidos en los constantes. El texto fuera de los delimitadores
 * se muestra sin estilo adicional.
 *
 * @param text Cadena de texto con delimitadores de estilo.
 * @return AnnotatedString con los estilos aplicados.
 */
fun buildAnnotatedStringStyled(text: String) = buildAnnotatedString {
    return buildAnnotatedString {
        val regex = Regex(REGEX_STYLED_TEXT_PATTERN)
        var lastIndex = ZERO
        for (match in regex.findAll(text)) {
            val start = match.range.first
            val end = match.range.last + ONE
            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }
            val style = when (match.groupValues[ONE]) {
                BOLD_STYLED_TEXT -> SpanStyle(fontWeight = FontWeight.Bold)
                SEMI_BOLD_STYLED_TEXT -> SpanStyle(fontWeight = FontWeight.SemiBold)
                ITALIC_STYLED_TEXT -> SpanStyle(fontStyle = FontStyle.Italic)
                UNDER_LINE_STYLED_TEXT -> SpanStyle(textDecoration = TextDecoration.Underline)
                LINE_THROUGH_STYLED_TEXT -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                else -> SpanStyle()
            }
            withStyle(style) {
                append(match.groupValues[TWO])
            }
            lastIndex = end
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}
