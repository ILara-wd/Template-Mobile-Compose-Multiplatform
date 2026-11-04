package com.invex.one.demo.ui.components.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.cpCard
import com.invex.one.demo.ui.theme.CpError
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpOutline
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSecondaryContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerHigh
import com.invex.one.demo.ui.theme.CpSurfaceContainerHighest
import com.invex.one.demo.ui.theme.CpSurfaceContainerLow
import com.invex.one.demo.ui.theme.CpTextSecondary

val CpSectionShape = RoundedCornerShape(12.dp)
private val FieldShape = RoundedCornerShape(8.dp)

// ---------- Etiquetas ----------

/** Etiqueta de campo con indicador obligatorio (*) en rojo. */
@Composable
fun RequiredLabel(text: String, modifier: Modifier = Modifier, required: Boolean = true) {
    Row(
        modifier.semantics { heading() },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall, color = CpOnSurface)
        if (required) Text("*", style = MaterialTheme.typography.titleSmall, color = CpError)
    }
}

/** Tarjeta de sección de formulario (radio 12dp, p16, gap 12) con encabezado. */
@Composable
fun FormSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    required: Boolean = true,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier.fillMaxWidth().cpCard(CpSectionShape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RequiredLabel(title, required = required)
            trailing?.invoke(this)
        }
        content()
    }
}

/** Metadato pequeño a la derecha del encabezado ("Selección obligatoria", "R-410A"). */
@Composable
fun SectionMeta(text: String, color: Color = CpTextSecondary) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
}

// ---------- M8. Stepper móvil de 3 pasos ----------

/**
 * Indicador horizontal del flujo de captura. El paso activo ocupa el espacio
 * libre y muestra su estado; los demás se atenúan al 60%.
 */
@Composable
fun ServiceStepper(
    steps: List<String>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
    activeCaption: String = "Activo",
) {
    Row(
        modifier.fillMaxWidth().cpCard(CpSectionShape).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.forEachIndexed { index, label ->
            if (index > 0) {
                Box(Modifier.width(32.dp).padding(horizontal = 4.dp)) {
                    Box(
                        Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .background(CpSurfaceContainerHighest, CircleShape),
                    )
                }
            }
            if (index == currentIndex) {
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .shadow(1.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.05f))
                            .background(CpPrimary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Column {
                        Text(label, style = MaterialTheme.typography.titleSmall, color = CpPrimary, maxLines = 1)
                        Text(activeCaption, style = MaterialTheme.typography.labelSmall, color = CpTextSecondary, maxLines = 1)
                    }
                }
            } else {
                Row(
                    Modifier.alpha(0.6f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(24.dp).background(CpSurfaceContainerHigh, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CpOnSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Text(label, style = MaterialTheme.typography.labelMedium, color = CpOnSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

// ---------- Selectores ----------

/**
 * Selector desplegable de registro (cliente, equipo): icono en contenedor de
 * 36dp, título, detalle truncado e indicador "unfold".
 */
@Composable
fun SelectorField(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContainerColor: Color = CpSecondaryContainer,
    iconContainerShape: Shape = CircleShape,
    onClickLabel: String? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .clip(FieldShape)
            .background(CpSurfaceContainerLow)
            .clickable(role = Role.Button, onClickLabel = onClickLabel, onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(36.dp).background(iconContainerColor, iconContainerShape),
                contentAlignment = Alignment.Center,
            ) {
                CpIcon(icon)
            }
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = CpOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = CpOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        CpIcon(R.drawable.ic_unfold_more)
    }
}

/**
 * Opción de radio en grilla (56dp). Seleccionada: fondo azul tonal 50%, icono
 * primario y check; inactiva: fondo gris y círculo vacío.
 */
@Composable
fun ServiceTypeOption(
    label: String,
    @DrawableRes icon: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Azul tonal al 50% compuesto sobre la tarjeta blanca: un fondo translúcido
    // dejaría ver el relleno de la sombra de elevación.
    val container = if (selected) {
        CpSecondaryContainer.copy(alpha = 0.5f).compositeOver(Color.White)
    } else {
        CpSurfaceContainerLow
    }
    Row(
        modifier
            .height(56.dp)
            .then(
                if (selected) {
                    Modifier.shadow(1.dp, FieldShape, spotColor = Color.Black.copy(alpha = 0.05f))
                } else {
                    Modifier
                },
            )
            .clip(FieldShape)
            .background(container)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (selected) CpPrimary else CpOnSurfaceVariant,
        )
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = CpOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            CpIcon(R.drawable.ic_radio_checked)
        } else {
            Box(Modifier.size(16.dp).background(CpSurfaceContainerHighest, CircleShape))
        }
    }
}

/** Campo de selección de fecha/hora (48dp) con etiqueta superior. */
@Composable
fun PickerField(
    label: String,
    value: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = CpOnSurfaceVariant, maxLines = 1)
        Row(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(FieldShape)
                .background(CpSurfaceContainerLow)
                .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CpIcon(icon)
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = CpOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            CpIcon(R.drawable.ic_expand_more)
        }
    }
}

/** Campo multilínea opcional con etiqueta, marca "Opcional" e icono de edición. */
@Composable
fun NotesField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    optionalText: String = "Opcional",
) {
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = CpOnSurface)
    Column(
        modifier.fillMaxWidth().padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = CpOnSurfaceVariant)
            Text(optionalText, style = MaterialTheme.typography.labelSmall, color = CpOutline)
        }
        Box(Modifier.fillMaxWidth().padding(bottom = 5.dp)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = textStyle,
                cursorBrush = SolidColor(CpPrimaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CpSurfaceContainerLow, FieldShape)
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 32.dp),
            )
            CpIcon(
                R.drawable.ic_edit_note,
                Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 12.dp),
            )
        }
    }
}

/** Mini banner informativo: "Campos con (*) son obligatorios…". */
@Composable
fun RequiredFieldsHint(
    prefix: String,
    suffix: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CpIcon(R.drawable.ic_info)
        Text(
            buildAnnotatedString {
                append(prefix)
                withStyle(SpanStyle(color = CpError, fontWeight = FontWeight.SemiBold)) { append("*") }
                append(suffix)
            },
            style = MaterialTheme.typography.bodySmall,
            color = CpOnSurfaceVariant,
        )
    }
}
