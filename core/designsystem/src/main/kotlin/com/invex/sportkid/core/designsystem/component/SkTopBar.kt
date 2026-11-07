package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.R
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme

/**
 * O2. Top bar de detalle o formulario: atrás, título y acciones a la derecha.
 * Fondo translúcido para que el contenido se vea al hacer scroll por debajo.
 */
@Composable
fun SkDetailTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        // Figma usa 0.8 + backdrop blur; sin blur (no disponible antes de Android 12) se sube a 0.95.
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = SkElevation.level1,
    ) {
        Row(
            Modifier
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = SportKidTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painterResource(R.drawable.sk_ic_arrow_back),
                    contentDescription = stringResource(R.string.sk_action_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.width(SportKidTheme.spacing.xs))
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            actions()
        }
    }
}

/** Avatar del usuario en la top bar (círculo de 32dp `primary`). Abre Ajustes. */
@Composable
fun SkProfileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Box(
            Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.sk_ic_person),
                contentDescription = stringResource(R.string.sk_action_profile),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@SkPreviews
@Composable
private fun SkDetailTopBarPreview() = SportKidTheme {
    SkDetailTopBar(title = "Crear deportista", onBack = {}) {
        SkProfileButton(onClick = {})
    }
}
