package com.invex.one.demo.ui.components.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.atoms.cpCard
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpOutline
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSuccessSoft
import com.invex.one.demo.ui.theme.CpSuccessStrong
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerHigh
import com.invex.one.demo.ui.theme.CpSurfaceContainerLow
import com.invex.one.demo.ui.theme.CpWarningContainer
import com.invex.one.demo.ui.theme.CpWarningStrong

private val Radius8 = RoundedCornerShape(8.dp)

// ---------- M8. Stepper compacto con estado completado ----------

/**
 * Stepper de 3 pasos con pasos completados (verde + check), activo (azul) y
 * pendientes (gris). Cada paso ocupa el mismo ancho; el último se alinea al final.
 */
@Composable
fun ServiceStepperCompact(steps: List<String>, currentIndex: Int, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().cpCard(CpSectionShape).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.forEachIndexed { i, label ->
            if (i > 0) {
                Box(Modifier.width(32.dp).padding(horizontal = 4.dp)) {
                    Box(
                        Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .background(if (i <= currentIndex) CpSuccessStrong else CpSurfaceContainerHigh),
                    )
                }
            }
            val done = i < currentIndex
            val active = i == currentIndex
            Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp, if (i == steps.lastIndex) Alignment.End else Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(24.dp)
                        .background(
                            when {
                                done -> CpSuccessSoft
                                active -> CpPrimaryContainer
                                else -> CpSurfaceContainerHigh
                            },
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) {
                        CpIcon(R.drawable.ic_check_success)
                    } else {
                        Text(
                            "${i + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) Color.White else CpOnSurfaceVariant,
                        )
                    }
                }
                Text(
                    "${i + 1}. $label",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                    ),
                    color = when {
                        done -> CpOnSurface
                        active -> CpPrimary
                        else -> CpOutline
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ---------- M5. Elemento de checklist ----------

/** Tarea técnica con checkbox de 24dp; al completarse se tacha y muestra check verde. */
@Composable
fun ChecklistTaskItem(
    title: String,
    done: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    pendingLabel: String = "Pendiente",
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(Radius8)
            .background(if (done) CpSurfaceContainerLow else CpBackground)
            .toggleable(value = done, role = Role.Checkbox, onValueChange = onToggle)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .background(if (done) CpSuccessStrong else CpSurfaceContainerHigh, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (done) CpIcon(R.drawable.ic_checkbox_check)
        }
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
            ),
            color = if (done) CpOutline else CpOnSurface,
            modifier = Modifier.weight(1f),
        )
        if (done) {
            CpIcon(R.drawable.ic_metric_week, tint = CpSuccessStrong)
        } else {
            StatusBadge(pendingLabel, CpWarningContainer, CpWarningStrong)
        }
    }
}

// ---------- M6. Evidencias antes / después ----------

/** Encabezado de sección de evidencias: badge de fase, título, subtítulo y conteo. */
@Composable
fun EvidenceSectionHeader(
    badge: String,
    badgeContainer: Color,
    badgeContent: Color,
    title: String,
    subtitle: String,
    count: Int,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                Modifier.semantics(mergeDescendants = true) { heading() },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusBadge(badge, badgeContainer, badgeContent, shape = RoundedCornerShape(4.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = CpOnSurface)
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = CpOnSurfaceVariant)
        }
        Text(
            if (count == 1) "1 foto" else "$count fotos",
            style = MaterialTheme.typography.labelSmall,
            color = CpOutline,
        )
    }
}

/** Miniatura de evidencia (128dp) con degradado, botón de quitar y pie de foto. */
@Composable
fun EvidenceThumbnail(
    image: Painter?,
    caption: String,
    @DrawableRes captionIcon: Int,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.height(128.dp).clip(Radius8).background(CpSurfaceContainer)) {
        if (image != null) {
            Image(image, caption, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to CpOnSurface.copy(alpha = 0f),
                        0.5f to CpOnSurface.copy(alpha = 0f),
                        1f to CpOnSurface.copy(alpha = 0.8f),
                    ),
                ),
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(CpOnSurface.copy(alpha = 0.7f))
                .clickable(role = Role.Button, onClickLabel = "Quitar foto", onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            CpIcon(R.drawable.ic_close_white)
        }
        Row(
            Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CpIcon(captionIcon, tint = Color.White)
            Text(
                caption,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Botón ancho (64dp) "+ Tomar o agregar foto". */
@Composable
fun AddPhotoButton(onClick: () -> Unit, modifier: Modifier = Modifier, text: String = "+ Tomar o agregar foto") {
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(CpSectionShape)
            .background(CpSurfaceContainerLow)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(32.dp).background(CpSurfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
            CpIcon(R.drawable.ic_add_a_photo)
        }
        Text(
            text,
            style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.14.sp),
            color = CpOnSurface,
        )
    }
}

/** Celda de la grilla (128dp) para agregar una foto directamente. */
@Composable
fun AddPhotoTile(onClick: () -> Unit, modifier: Modifier = Modifier, text: String = "+ Tomar o agregar foto") {
    Column(
        modifier
            .height(128.dp)
            .clip(Radius8)
            .background(CpSurfaceContainerLow)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 5.33.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(36.dp).background(CpSurfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
            CpIcon(R.drawable.ic_add_a_photo_accent)
        }
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = CpOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}
