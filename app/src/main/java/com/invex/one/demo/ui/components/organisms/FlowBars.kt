package com.invex.one.demo.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpButton
import com.invex.one.demo.ui.components.atoms.CpCardShape
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusDot
import com.invex.one.demo.ui.components.atoms.cpCard
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainer

/** Franja auxiliar de flujo: punto de estado, título y botón circular de cierre. */
@Composable
fun FlowHeaderStrip(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    closeLabel: String = "Cancelar y cerrar",
) {
    Row(
        modifier
            .fillMaxWidth()
            .cpCard(RectangleShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(CpPrimary, size = 10.dp)
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = CpOnSurface,
                modifier = Modifier.semantics { heading() },
            )
        }
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CpSurfaceContainer)
                .clickable(role = Role.Button, onClickLabel = closeLabel, onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            CpIcon(R.drawable.ic_close)
        }
    }
}

/**
 * Barra inferior persistente de acciones del flujo: secundaria "Cancelar" +
 * primaria con flecha hacia el siguiente paso.
 */
@Composable
fun FlowActionBar(
    primaryText: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String = "Cancelar",
) {
    Box(
        modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.06f),
            )
            .background(Color.White.copy(alpha = 0.95f))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.widthIn(max = 512.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CpButton(
                text = secondaryText,
                onClick = onSecondary,
                contentColor = CpOnSurface,
                containerColor = CpSurfaceContainer,
                height = 48.dp,
                horizontalPadding = 16.dp,
                textStyle = MaterialTheme.typography.labelLarge,
            )
            CpButton(
                text = primaryText,
                onClick = onPrimary,
                contentColor = Color.White,
                containerColor = CpPrimaryContainer,
                height = 48.dp,
                horizontalPadding = 20.dp,
                gap = 8.dp,
                textStyle = MaterialTheme.typography.labelLarge,
                trailingIcon = R.drawable.ic_arrow_forward_white,
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = 4.dp,
                        shape = CpCardShape,
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f),
                    ),
            )
        }
    }
}
