package com.invex.one.demo.ui.components.organisms

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.theme.CpBarScrim
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer

/**
 * Logo CierrePro: wordmark de fondo + isotipo recortado encima (según Figma).
 * El isotipo vive en un contenedor [markSize] desplazado 1dp hacia abajo y se
 * escala al 147.54% × 155.17% con desfase (-24.12%, -27.37%).
 */
@Composable
fun CierreProLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    markSize: DpSize = DpSize(32.dp, 30.dp),
) {
    Box(modifier.size(size).clipToBounds()) {
        Image(
            painter = painterResource(R.drawable.logo_cierrepro_wordmark),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.offset(y = 1.dp).size(markSize).clipToBounds()) {
            Image(
                painter = painterResource(R.drawable.logo_cierrepro_mark),
                contentDescription = "CierrePro",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .offset(x = markSize.width * -0.2412f, y = markSize.height * -0.2737f)
                    .requiredSize(markSize.width * 1.4754f, markSize.height * 1.5517f),
            )
        }
    }
}

/** Contenedor común de las Top Bars: blanco 90% con sombra suave y status bar. */
@Composable
private fun TopBarSurface(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.04f),
            )
            .background(CpBarScrim)
            .statusBarsPadding(),
    ) { content() }
}

/** Avatar de acceso a "Mi Negocio" (32dp, primario). */
@Composable
private fun ProfileButton(onClick: () -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(CpPrimary)
            .clickable(role = Role.Button, onClickLabel = "Mi Negocio", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        CpIcon(R.drawable.ic_profile)
    }
}

/** O1. Top Bar de aplicación (blanco 90% con sombra suave). */
@Composable
fun CierreProTopBar(
    sectionTitle: String,
    onProfileClick: () -> Unit,
) {
    TopBarSurface {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CierreProLogo()
                Column {
                    Text(
                        "CierrePro",
                        style = MaterialTheme.typography.titleSmall.copy(lineHeight = 17.5.sp),
                        color = CpOnSurface,
                    )
                    Text(
                        sectionTitle,
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 11.sp),
                        color = CpOnSurfaceVariant,
                    )
                }
            }
            ProfileButton(onProfileClick)
        }
    }
}

/** O1b. Top Bar de detalle: botón de regreso, logo 28dp y título de pantalla. */
@Composable
fun CierreProDetailTopBar(
    title: String,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
) {
    TopBarSurface {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.weight(1f, fill = false),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClickLabel = "Regresar", onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    CpIcon(R.drawable.ic_arrow_back)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CierreProLogo(size = 28.dp, markSize = DpSize(27.dp, 25.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CpOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() },
                    )
                }
            }
            ProfileButton(onProfileClick)
        }
    }
}

enum class CpDestination(val label: String, @DrawableRes val icon: Int) {
    Inicio("Inicio", R.drawable.ic_nav_home),
    Clientes("Clientes", R.drawable.ic_nav_clients),
    Servicios("Servicios", R.drawable.ic_nav_services),
    Seguimientos("Seguimientos", R.drawable.ic_nav_followups),
}

/** O2. Bottom Navigation persistente de 4 destinos. */
@Composable
fun CierreProBottomBar(
    selected: CpDestination,
    onSelect: (CpDestination) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f),
            )
            .background(CpBarScrim)
            .navigationBarsPadding(),
    ) {
        Row(
            Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp).selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CpDestination.entries.forEach { dest ->
                val isSelected = dest == selected
                val color = if (isSelected) CpPrimaryContainer else CpOnSurfaceVariant
                Column(
                    Modifier
                        .weight(1f)
                        .defaultMinSize(minWidth = 56.dp, minHeight = 48.dp)
                        .clip(MaterialTheme.shapes.small)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(dest) })
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(painterResource(dest.icon), contentDescription = null, tint = color)
                    Text(
                        dest.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}
