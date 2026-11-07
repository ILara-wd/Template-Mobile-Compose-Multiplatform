package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.R
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.designsystem.theme.metric

/**
 * Encabezado de sección de formulario: texto `titleSmall`, asterisco si es obligatorio, icono
 * líder opcional y slot a la derecha (ayuda, icono decorativo).
 */
@Composable
fun SkFormLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    icon: Painter? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val requiredLabel = stringResource(R.string.sk_required)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = colors.primary)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            buildAnnotatedString {
                append(text)
                if (required) withStyle(SpanStyle(color = colors.tertiary)) { append(" *") }
            },
            modifier = Modifier
                .weight(1f)
                .semantics { if (required) contentDescription = "$text, $requiredLabel" },
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurface,
        )
        trailing?.invoke(this)
    }
}

/**
 * M7. Campo de dinero compacto: prefijo "$", monto en `metric` alineado a la derecha y sufijo de
 * moneda. Solo acepta dígitos (pesos enteros).
 */
@Composable
fun SkMoneyField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    currency: String = "MXN",
    amountWidth: Dp = 64.dp,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .height(40.dp)
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.small)
            .padding(horizontal = SportKidTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$", style = MaterialTheme.typography.titleSmall, color = colors.onSurfaceVariant)
        BasicTextField(
            value = value,
            onValueChange = { new -> onValueChange(new.filter(Char::isDigit).take(7)) },
            modifier = Modifier.width(amountWidth),
            singleLine = true,
            textStyle = MaterialTheme.typography.metric.copy(color = colors.onSurface, textAlign = TextAlign.End),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Text(
            currency,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
            color = colors.onSurfaceVariant,
        )
    }
}

/**
 * Campo multilínea (mínimo 96dp) con pie opcional para metadatos y acciones
 * (Figma: "Notas y equipo para tutores").
 */
@Composable
fun SkTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    footer: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 96.dp)
            .background(colors.surfaceContainerHigh, MaterialTheme.shapes.medium)
            .padding(SportKidTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            decorationBox = { inner ->
                if (value.isEmpty() && placeholder != null) {
                    Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = colors.outline)
                }
                inner()
            },
        )
        if (footer != null) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                content = footer,
            )
        }
    }
}

/**
 * FAB extendido de acción principal (Figma: "+ Nuevo evento"). Pill `tertiary`, el color de
 * "logros y energía".
 */
@Composable
fun SkExtendedFab(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
) {
    ExtendedFloatingActionButton(
        text = { Text(text, style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.35.sp)) },
        icon = { Icon(painterResource(R.drawable.sk_ic_add), contentDescription = null) },
        onClick = onClick,
        modifier = modifier,
        expanded = expanded,
        shape = MaterialTheme.shapes.full,
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = SkElevation.level3),
    )
}

@SkPreviews
@Composable
private fun SkFormFieldsPreview() = SportKidTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SkFormLabel("Lugar del entrenamiento", required = true)
        SkMoneyField(value = "250", onValueChange = {})
        SkTextArea(value = "", onValueChange = {}, placeholder = "Ej. Llevar dobok limpio…")
        SkExtendedFab(text = "Nuevo evento", onClick = {})
    }
}
