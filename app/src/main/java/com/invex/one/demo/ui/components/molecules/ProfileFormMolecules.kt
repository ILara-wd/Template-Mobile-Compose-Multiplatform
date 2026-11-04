package com.invex.one.demo.ui.components.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.theme.CpError
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerLow

private val FieldShape = RoundedCornerShape(8.dp)

/**
 * M2. Campo con validación: etiqueta (obligatorio * u "(opcional)") con meta
 * opcional a la derecha, input de 48dp con icono leading y acción trailing
 * opcionales (p. ej. ver contraseña) y texto auxiliar o de error.
 */
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    optional: Boolean = false,
    @DrawableRes leadingIcon: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    error: String? = null,
    helper: (@Composable () -> Unit)? = null,
    labelWeight: FontWeight = FontWeight.Medium,
    labelTrailing: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingAction: (@Composable () -> Unit)? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val labelStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = labelWeight)
    val fieldTextStyle = textStyle.copy(color = CpOnSurface)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                buildAnnotatedString {
                    append(label)
                    if (required) withStyle(SpanStyle(color = CpError)) { append(" *") }
                    if (optional) {
                        withStyle(SpanStyle(color = CpOnSurfaceVariant, fontWeight = FontWeight.Normal)) { append(" (opcional)") }
                    }
                },
                style = labelStyle,
                color = CpOnSurface,
            )
            labelTrailing?.invoke()
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = fieldTextStyle,
            cursorBrush = SolidColor(CpPrimaryContainer),
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                autoCorrectEnabled = keyboardType != KeyboardType.Password && keyboardType != KeyboardType.Email,
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .then(if (singleLine) Modifier.height(48.dp) else Modifier.defaultMinSize(minHeight = 48.dp))
                        .clip(FieldShape)
                        .background(CpSurfaceContainerLow)
                        .padding(start = if (leadingIcon != null) 15.67.dp else 14.dp, end = if (trailingAction != null) 4.dp else 14.dp)
                        .padding(vertical = if (singleLine) 0.dp else 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(11.67.dp),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                ) {
                    leadingIcon?.let { CpIcon(it) }
                    Box(Modifier.weight(1f)) { inner() }
                    trailingAction?.invoke()
                }
            },
        )
        when {
            error != null -> Text(error, style = MaterialTheme.typography.bodySmall, color = CpError)
            helper != null -> helper()
        }
    }
}

/** Icono + texto auxiliar bajo un campo. */
@Composable
fun FieldHint(@DrawableRes icon: Int, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier.padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CpIcon(icon)
        Text(text, style = MaterialTheme.typography.bodySmall, color = CpOnSurfaceVariant)
    }
}

/** Miniatura cuadrada del logotipo (recorte centrado) o marcador vacío. */
@Composable
fun LogoThumbnail(
    logo: Painter?,
    size: Dp,
    shape: Shape,
    modifier: Modifier = Modifier,
    background: Color = CpSurfaceContainer,
) {
    Box(
        modifier.size(size).clip(shape).background(background),
        contentAlignment = Alignment.Center,
    ) {
        if (logo != null) {
            Image(
                painter = logo,
                contentDescription = "Logotipo del negocio",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CpIcon(R.drawable.ic_letterhead)
        }
    }
}
