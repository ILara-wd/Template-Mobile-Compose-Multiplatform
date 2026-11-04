package com.invex.one.demo.ui.components.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.atoms.cpCard
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpOutline
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSecondaryContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerLow
import com.invex.one.demo.ui.theme.CpTextSecondary

private val Radius12 = RoundedCornerShape(12.dp)

/** M1. Buscador táctil (48dp, radio 12dp, icono leading). */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val textStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = 17.sp, color = CpOnSurface)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(CpPrimaryContainer),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Row(
                Modifier.fillMaxWidth().height(48.dp).cpCard(Radius12).padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CpIcon(R.drawable.ic_search)
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = textStyle, color = CpOutline, maxLines = 1)
                    }
                    inner()
                }
            }
        },
    )
}

/** Chip de filtro pill (36dp) con contador opcional. */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    val labelStyle = MaterialTheme.typography.labelLarge.copy(
        letterSpacing = 0.14.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
    )
    Row(
        modifier
            .height(36.dp)
            .cpCard(CircleShape)
            .clip(CircleShape)
            .background(if (selected) CpSecondaryContainer else Color.White)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = labelStyle, color = if (selected) CpPrimary else CpTextSecondary, maxLines = 1)
        if (count != null) {
            Box(
                Modifier
                    .height(20.dp)
                    .defaultMinSize(minWidth = 20.dp)
                    .background(CpPrimaryContainer, CircleShape)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}

/** Avatar circular con iniciales (44dp en listas, 56dp en ficha de cliente). */
@Composable
fun InitialsAvatar(
    initials: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Box(
        modifier.size(size).background(containerColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, style = textStyle, color = contentColor)
    }
}

/** Badge de estado de cliente (Seguimiento programado / Al día / Mantenimiento pendiente). */
@Composable
fun ClientStatusBadge(
    text: String,
    @DrawableRes icon: Int,
    containerColor: Color,
    contentColor: Color,
) {
    StatusBadge(
        text = text,
        containerColor = containerColor,
        contentColor = contentColor,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        textStyle = MaterialTheme.typography.labelMedium,
        leading = { CpIcon(icon) },
    )
}

/** M4. Tarjeta de cliente: avatar, contacto, último servicio, estado y llamada. */
@Composable
fun ClientCard(
    name: String,
    phone: String,
    location: String,
    avatar: @Composable () -> Unit,
    @DrawableRes serviceIcon: Int,
    lastService: String,
    lastServiceWhen: String,
    status: @Composable () -> Unit,
    onClick: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier,
    contactName: String? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .cpCard(Radius12)
            .clip(Radius12)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Row(
                Modifier.weight(1f).padding(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                avatar()
                Column(verticalArrangement = Arrangement.spacedBy(if (contactName == null) 2.dp else 0.dp)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        color = CpOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (contactName != null) {
                        Text(
                            contactName,
                            style = MaterialTheme.typography.labelSmall,
                            color = CpOutline,
                            maxLines = 1,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val meta = MaterialTheme.typography.bodySmall
                        CpIcon(R.drawable.ic_phone_small)
                        Text(phone, style = meta, color = CpOnSurfaceVariant, maxLines = 1)
                        Text("•", style = meta, color = CpOutline, modifier = Modifier.padding(horizontal = 2.dp))
                        Text(
                            location,
                            style = meta,
                            color = CpOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
            }
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CpSurfaceContainerLow)
                    .clickable(role = Role.Button, onClickLabel = "Llamar a $name", onClick = onCall),
                contentAlignment = Alignment.Center,
            ) {
                CpIcon(R.drawable.ic_call)
            }
        }

        Row(
            Modifier.fillMaxWidth().background(CpSurfaceContainerLow, RoundedCornerShape(8.dp)).padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CpIcon(serviceIcon)
            Text(
                buildAnnotatedString {
                    append(lastService)
                    withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = CpOutline)) {
                        append(" • $lastServiceWhen")
                    }
                },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = CpOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            status()
            CpIcon(R.drawable.ic_chevron_right_card)
        }
    }
}
