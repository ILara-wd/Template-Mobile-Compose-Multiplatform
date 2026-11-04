package com.invex.one.demo.ui.components.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.ui.components.atoms.CpIcon
import kotlin.math.roundToInt
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.atoms.StatusDot
import com.invex.one.demo.ui.components.atoms.cpCard
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerHigh
import com.invex.one.demo.ui.theme.CpSurfaceContainerLow
import com.invex.one.demo.ui.theme.CpTextSecondary

private val Radius8 = RoundedCornerShape(8.dp)

// ---------- Filtros ----------

/** Chip de filtro con contador de color semántico (Todos / Para hoy / Próximos / Atrasados). */
@Composable
fun CountFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    countContainer: Color,
    countContent: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .then(if (selected) Modifier.shadow(1.dp, CircleShape, spotColor = Color.Black.copy(0.05f)) else Modifier)
            .clip(CircleShape)
            .background(if (selected) CpPrimaryContainer else CpSurfaceContainer)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else CpOnSurfaceVariant,
            maxLines = 1,
        )
        StatusBadge(
            text = count.toString(),
            containerColor = if (selected) Color.White.copy(alpha = 0.2f) else countContainer,
            contentColor = if (selected) Color.White else countContent,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = if (selected) 2.dp else 0.dp),
        )
    }
}

// ---------- Meta de re-contacto ----------

/** Anillo de progreso de 40dp (pista gris, arco primario desde las 12 en punto). */
@Composable
fun ProgressRing(progress: Float, modifier: Modifier = Modifier) {
    Box(modifier.size(40.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(40.dp)) {
            val stroke = 3.333.dp.toPx()
            val inset = 3.333.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            drawArc(CpSurfaceContainerHigh, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(
                CpPrimary,
                -90f,
                360f * progress.coerceIn(0f, 1f),
                false,
                Offset(inset, inset),
                arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Text("${(progress.coerceIn(0f, 1f) * 100).roundToInt()}%", style = MaterialTheme.typography.labelSmall, color = CpPrimary)
    }
}

/** Banner de meta diaria con tono de logro. */
@Composable
fun GoalProgressBanner(
    title: String,
    subtitle: String,
    progress: Float,
    @DrawableRes trailingIcon: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().cpCard(CpSectionShape).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProgressRing(progress)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = CpOnSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = CpOnSurfaceVariant)
        }
        CpIcon(trailingIcon)
    }
}

// ---------- Grupos ----------

/** Encabezado de grupo: punto de color, título y meta/pill a la derecha. */
@Composable
fun FollowUpGroupHeader(
    title: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
    titleColor: Color = CpOnSurface,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.semantics(mergeDescendants = true) { heading() },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(dotColor)
            Text(title, style = MaterialTheme.typography.titleSmall, color = titleColor)
        }
        trailing?.invoke()
    }
}

// ---------- Tarjeta de seguimiento ----------

/**
 * Tarjeta de seguimiento: avatar con icono, nombre (+etiqueta opcional),
 * línea de contacto/fecha, elemento a la derecha, motivo y acciones.
 * [accentColor] dibuja la franja lateral de alerta (6dp) de los atrasados.
 */
@Composable
fun FollowUpCard(
    name: String,
    @DrawableRes avatarIcon: Int,
    avatarContainer: Color,
    subline: @Composable () -> Unit,
    reason: @Composable ColumnScope.() -> Unit,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    tag: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    accentColor: Color? = null,
) {
    val inner = if (accentColor != null) 4.dp else 0.dp
    Box(
        modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .cpCard(CpSectionShape)
            .clip(CpSectionShape),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = inner),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(44.dp).background(avatarContainer, CircleShape), contentAlignment = Alignment.Center) {
                        CpIcon(avatarIcon)
                    }
                    Column(Modifier.weight(1f, fill = false)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                name,
                                style = MaterialTheme.typography.titleMedium,
                                color = CpOnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false).semantics { heading() },
                            )
                            if (tag != null) {
                                StatusBadge(
                                    text = tag,
                                    containerColor = CpTextSecondary,
                                    contentColor = Color.White,
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp),
                                    textStyle = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        lineHeight = 20.sp,
                                        letterSpacing = 0.sp,
                                    ),
                                )
                            }
                        }
                        subline()
                    }
                }
                trailing?.invoke()
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = inner)
                    .background(CpSurfaceContainerLow, Radius8)
                    .padding(12.dp),
                content = reason,
            )
            Box(Modifier.fillMaxWidth().padding(start = inner, top = 4.dp)) { actions() }
        }
        if (accentColor != null) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(accentColor))
        }
    }
}

/** Icono + texto pequeño (teléfono, fecha sugerida, vencimiento, notas). */
@Composable
fun IconLine(
    @DrawableRes icon: Int,
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.SemiBold,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        CpIcon(icon)
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = fontWeight),
            color = color,
        )
    }
}
