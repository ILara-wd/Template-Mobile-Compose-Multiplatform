package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.R
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme

/**
 * Sección de Ajustes: card blanca de 16dp con encabezado (icono y título en MAYÚSCULAS),
 * [badge] opcional a la derecha ("FMTKD", "Al día") y sus filas.
 */
@Composable
fun SettingsSection(
    title: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    badge: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = SkElevation.card) {
        Column(Modifier.padding(vertical = SportKidTheme.spacing.lg)) {
            Row(
                Modifier
                    .padding(horizontal = SportKidTheme.spacing.lg)
                    .padding(bottom = SportKidTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                Text(
                    title.uppercase(),
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                    style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.7.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                badge?.invoke()
            }
            Column(Modifier.padding(horizontal = SportKidTheme.spacing.sm), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs), content = content)
        }
    }
}

/**
 * M27. Fila de ajustes: icono en círculo, título, subtítulo opcional y lo de la derecha.
 *
 * - Con [checked] muestra un [SkSwitch] y toda la fila lo alterna (role Switch).
 * - Si no, con [onClick] y sin [trailing] muestra un chevron.
 * - [trailing] sustituye lo anterior (valor en pill, muestra de color + chevron).
 */
@Composable
fun SettingsRow(
    title: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconContainerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconContainerSize: Dp = 40.dp,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val interaction = when {
        checked != null && onCheckedChange != null -> Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
        onClick != null -> Modifier.clickable(role = Role.Button, onClick = onClick)
        else -> Modifier
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(interaction)
            .padding(SportKidTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkIconCircle(icon = icon, contentDescription = null, size = iconContainerSize, containerColor = iconContainerColor, iconTint = iconTint)
        Spacer(Modifier.width(SportKidTheme.spacing.md))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        when {
            trailing != null -> trailing()
            checked != null -> SkSwitch(checked = checked, onCheckedChange = null)
            onClick != null -> Icon(painterResource(R.drawable.sk_ic_chevron_right), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@SkPreviews
@Composable
private fun SettingsPreview() = SportKidTheme {
    SettingsSection(title = "Notificaciones y alertas", icon = painterResource(R.drawable.sk_ic_notifications), modifier = Modifier.padding(16.dp)) {
        SettingsRow("Eventos y pagos pendientes", painterResource(R.drawable.sk_ic_notifications), subtitle = "Torneos, colegiaturas y citas", checked = true, onCheckedChange = {})
        SettingsRow("Anticipación de aviso", painterResource(R.drawable.sk_ic_info), subtitle = "2 días antes del evento (09:00 hrs)", onClick = {})
    }
}
