package com.invex.one.demo.ui.components.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.atoms.cpCard
import com.invex.one.demo.ui.theme.CpInverseOnSurface
import com.invex.one.demo.ui.theme.CpInverseSurface
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerLow
import com.invex.one.demo.ui.theme.CpTextSecondary

private val Radius8 = RoundedCornerShape(8.dp)
private val Radius4 = RoundedCornerShape(4.dp)

/** Rótulo de sección del documento (mayúsculas, 11sp, tracking 0.55). */
@Composable
fun DocSectionLabel(text: String, modifier: Modifier = Modifier, color: Color = CpTextSecondary) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.semantics { heading() },
    )
}

/** Fila de encabezado de sección: rótulo a la izquierda y meta a la derecha. */
@Composable
fun DocSectionHeader(label: String, trailing: (@Composable RowScope.() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DocSectionLabel(label, Modifier.weight(1f))
        trailing?.invoke(this)
    }
}

/** Bloque gris redondeado del documento (p12). */
@Composable
fun DocBlock(
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier.fillMaxWidth().background(CpSurfaceContainerLow, Radius8).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

/** Icono + texto de metadato (11sp). */
@Composable
fun DocMeta(
    @DrawableRes icon: Int,
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CpOnSurface,
    fontWeight: FontWeight = FontWeight.SemiBold,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        CpIcon(icon)
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = fontWeight),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Aviso de éxito: insignia circular, título, detalle y pill a la derecha. */
@Composable
fun SuccessBanner(
    title: String,
    subtitle: String,
    badge: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().cpCard(CpSectionShape).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(32.dp).background(CpSurfaceContainer, CircleShape), contentAlignment = Alignment.Center) {
                CpIcon(R.drawable.ic_verified)
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = CpOnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = CpTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        StatusBadge(text = badge, containerColor = CpSurfaceContainer, contentColor = CpOnSurfaceVariant)
    }
}

/** Paso del protocolo técnico ejecutado, con lectura de medición opcional. */
@Composable
fun ChecklistResultItem(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    reading: String? = null,
) {
    Row(
        modifier.fillMaxWidth().background(CpSurfaceContainerLow, Radius8).padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier.padding(top = 2.dp).size(20.dp).background(CpPrimary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CpIcon(R.drawable.ic_check_white)
        }
        Column(Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = CpOnSurface,
                    modifier = Modifier.weight(1f),
                )
                if (reading != null) {
                    Text(
                        reading,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CpPrimary,
                        modifier = Modifier
                            .background(CpSurfaceContainer, Radius4)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Text(detail, style = MaterialTheme.typography.bodySmall, color = CpTextSecondary)
        }
    }
}

/** Fase de la evidencia fotográfica, con su color de badge y degradado. */
enum class EvidencePhase(val label: String, val tint: Color, val badgeText: Color, val gradientAlpha: Float) {
    Antes("Antes", CpInverseSurface, CpInverseOnSurface, 0.85f),
    Despues("Después", CpPrimary, Color.White, 0.9f),
}

/** Miniatura de evidencia (159dp) con degradado inferior, badge de fase y pie. */
@Composable
fun EvidencePhotoTile(
    @DrawableRes image: Int,
    phase: EvidencePhase,
    caption: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(159.dp)
            .clip(Radius8)
            .background(CpSurfaceContainer),
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = "${phase.label}: $caption",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to phase.tint.copy(alpha = 0f),
                        0.5f to phase.tint.copy(alpha = 0f),
                        1f to phase.tint.copy(alpha = phase.gradientAlpha),
                    ),
                ),
        )
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(8.dp)) {
            Text(
                phase.label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = phase.badgeText,
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .background(phase.tint, Radius4)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
            Text(
                caption,
                style = MaterialTheme.typography.labelSmall,
                color = phase.badgeText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Bloque de diagnóstico con título primario y texto enriquecido. */
@Composable
fun DiagnosisBlock(title: String, body: AnnotatedString, modifier: Modifier = Modifier) {
    DocBlock(modifier, spacing = 6.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            CpIcon(R.drawable.ic_verified_user)
            Text(title, style = MaterialTheme.typography.titleSmall, color = CpPrimary, modifier = Modifier.semantics { heading() })
        }
        Text(body, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 19.25.sp), color = CpOnSurface)
    }
}

/** Conformidad del cliente: trazo de firma, nombre y sello de aprobación. */
@Composable
fun SignatureBlock(
    label: String,
    mode: String,
    @DrawableRes signature: Int,
    signerName: String,
    approval: String,
    modifier: Modifier = Modifier,
) {
    DocBlock(modifier) {
        DocSectionHeader(label) {
            Text(mode, style = MaterialTheme.typography.labelSmall, color = CpTextSecondary, maxLines = 1)
        }
        Column(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 90.dp)
                .background(Color.White, Radius8)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(painterResource(signature), contentDescription = "Firma de $signerName")
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(signerName, style = MaterialTheme.typography.labelSmall, color = CpOnSurface, maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    CpIcon(R.drawable.ic_check_circle_primary)
                    Text(approval, style = MaterialTheme.typography.labelSmall, color = CpPrimary, maxLines = 1)
                }
            }
        }
    }
}
