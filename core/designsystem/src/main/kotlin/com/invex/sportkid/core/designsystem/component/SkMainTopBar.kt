package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.R
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme

/**
 * O1. Top bar de las pantallas principales (Inicio, Agenda, Deportistas, Gastos): marca,
 * notificaciones y avatar del usuario, que abre Ajustes.
 *
 * Sin [avatar] se muestra el círculo `primary` con el icono de persona.
 */
@Composable
fun SkMainTopBar(
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatar: Painter? = null,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        // Figma: 0.85 + backdrop blur. Sin blur (Android < 12) se sube la opacidad, como en O2.
        color = colors.surface.copy(alpha = 0.95f),
        shadowElevation = SkElevation.level1,
    ) {
        Row(
            Modifier
                .statusBarsPadding()
                .height(64.dp)
                .padding(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.sk_brand_name),
                style = MaterialTheme.typography.titleLarge.copy(letterSpacing = (-0.5).sp),
                color = colors.primary,
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    painterResource(R.drawable.sk_ic_notifications),
                    contentDescription = stringResource(R.string.sk_action_notifications),
                    tint = colors.onSurfaceVariant,
                )
            }
            if (avatar == null) {
                SkProfileButton(onClick = onProfileClick)
            } else {
                IconButton(onClick = onProfileClick) {
                    SkAvatar(
                        photo = avatar,
                        size = 36.dp,
                        ringColor = colors.primaryContainer,
                        contentDescription = stringResource(R.string.sk_action_profile),
                    )
                }
            }
        }
    }
}

@SkPreviews
@Composable
private fun SkMainTopBarPreview() = SportKidTheme {
    SkMainTopBar(onNotificationsClick = {}, onProfileClick = {})
}
