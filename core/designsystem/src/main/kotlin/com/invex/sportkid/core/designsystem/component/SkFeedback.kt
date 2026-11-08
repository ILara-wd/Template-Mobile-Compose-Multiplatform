package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.R
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme

/**
 * M19. Callout informativo: icono, título opcional y texto. Sin título es una burbuja compacta
 * ("Torneo programado: el resultado se actualizará…"); con título y [emphasized] el icono va
 * en un círculo `primary` ("Actualización automática").
 *
 * Para otras variantes (icono en un círculo de otro color, texto neutro, card de 16dp) usa
 * [leading], [bodyColor], [shape] y [contentPadding]: "Sincronización en tiempo real" (P17),
 * "¿Dónde encuentro este código?" (P18) y roles del círculo familiar (P19).
 */
@Composable
fun SkInfoCallout(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: Painter = painterResource(R.drawable.sk_ic_info),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    emphasized: Boolean = false,
    bodyColor: Color? = null,
    shape: Shape? = null,
    contentPadding: Dp? = null,
    spacing: Dp = SportKidTheme.spacing.sm,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier.fillMaxWidth(),
        shape = shape ?: if (title != null) MaterialTheme.shapes.medium else MaterialTheme.shapes.small,
        color = containerColor,
    ) {
        Row(
            Modifier.padding(contentPadding ?: if (title != null) SportKidTheme.spacing.lg else SportKidTheme.spacing.sm),
            verticalAlignment = if (title != null || leading != null) Alignment.Top else Alignment.CenterVertically,
        ) {
            when {
                leading != null -> leading()
                emphasized -> SkIconCircle(icon = icon, contentDescription = null, size = 32.dp, containerColor = colors.primary, iconTint = colors.onPrimary)
                else -> Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.primary)
            }
            Spacer(Modifier.width(spacing))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(if (leading != null) 4.dp else 2.dp)) {
                if (title != null) Text(title, style = MaterialTheme.typography.titleSmall, color = contentColor)
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall,
                    color = bodyColor ?: if (title != null) colors.primary else contentColor,
                )
            }
        }
    }
}

@Composable
fun SkInfoCallout(
    text: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: Painter = painterResource(R.drawable.sk_ic_info),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    emphasized: Boolean = false,
) = SkInfoCallout(AnnotatedString(text), modifier, title, icon, containerColor, contentColor, emphasized)

/** Switch de M3 con check en el thumb cuando está encendido ("Pagado", "Cuota pagada"). */
@Composable
fun SkSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        thumbContent = if (checked) {
            { Icon(painterResource(R.drawable.sk_ic_swatch_check), contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
        } else {
            null
        },
    )
}

@SkPreviews
@Composable
private fun SkFeedbackPreview() = SportKidTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SkInfoCallout(
            "Al marcar como Aprobado, se actualizará la cinta actual en el perfil de Mateo.",
            title = "Actualización automática",
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            emphasized = true,
        )
        SkInfoCallout("Torneo programado: el resultado se actualizará al concluir los combates.")
        Row { SkSwitch(checked = true, onCheckedChange = {}); SkSwitch(checked = false, onCheckedChange = {}) }
    }
}
