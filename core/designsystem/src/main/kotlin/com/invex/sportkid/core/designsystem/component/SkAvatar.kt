package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.R
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme

/**
 * Avatar circular: foto o, si no hay, las iniciales de [name] (o el icono de persona) sobre
 * [placeholderColor]. El anillo opcional ocupa los [ringWidth] exteriores; la foto queda dentro.
 *
 * - Hijo: `ringColor` = su color de `AthleteColors` (README §1.2).
 * - Usuario en la top bar: `ringColor` = `primaryContainer`.
 * - Foto en el alta (P04): `ringBrush` degradado y [badge] de cámara.
 *
 * Tamaños habituales: 16 (chip), 32–36 (top bar), 40, 48 (selector), 56 y 96dp (perfil).
 */
@Composable
fun SkAvatar(
    photo: Painter?,
    modifier: Modifier = Modifier,
    name: String? = null,
    size: Dp = 40.dp,
    ringColor: Color? = null,
    ringBrush: Brush? = null,
    ringWidth: Dp = 2.dp,
    contentDescription: String? = null,
    placeholderColor: Color = MaterialTheme.colorScheme.primaryContainer,
    placeholderContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    shadowElevation: Dp = 0.dp,
    badge: (@Composable BoxScope.() -> Unit)? = null,
) {
    val ring = ringBrush ?: ringColor?.let(::SolidColor)
    Box(modifier) {
        Box(
            Modifier
                .size(size)
                .then(if (shadowElevation > 0.dp) Modifier.shadow(shadowElevation, CircleShape) else Modifier)
                .then(if (ring != null) Modifier.border(ringWidth, ring, CircleShape).padding(ringWidth) else Modifier)
                .clip(CircleShape)
                .background(placeholderColor)
                .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            val initials = name?.initials().orEmpty()
            when {
                photo != null -> Image(photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                initials.isNotEmpty() -> Text(initials, style = initialsStyle(size), color = placeholderContentColor, maxLines = 1)
                else -> Icon(
                    painterResource(R.drawable.sk_ic_person),
                    contentDescription = null,
                    tint = placeholderContentColor,
                    modifier = Modifier.size(size * 0.42f),
                )
            }
        }
        badge?.invoke(this)
    }
}

/** "Mateo Gómez" → "MG"; "Sofía" → "S". */
private fun String.initials(): String =
    trim().split(Regex("\\s+")).filter(String::isNotEmpty).take(2).joinToString("") { it.take(1).uppercase() }

@Composable
private fun initialsStyle(size: Dp): TextStyle = with(MaterialTheme.typography) {
    when {
        size <= 24.dp -> labelSmall
        size <= 40.dp -> labelLarge
        size <= 56.dp -> titleMedium
        else -> headlineSmall
    }
}

@SkPreviews
@Composable
private fun SkAvatarPreview() = SportKidTheme {
    val athlete = SportKidTheme.sportColors.athlete
    Row(
        Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkAvatar(photo = null, name = "Sofía", size = 16.dp)
        SkAvatar(photo = null, size = 36.dp, ringColor = MaterialTheme.colorScheme.primaryContainer)
        SkAvatar(photo = null, name = "Mateo Gómez", size = 48.dp, ringColor = athlete.blue)
        SkAvatar(photo = null, name = "Sofía", size = 56.dp, ringColor = athlete.pink)
        SkAvatar(
            photo = null,
            size = 96.dp,
            ringBrush = Brush.linearGradient(listOf(athlete.mint, athlete.sky)),
            ringWidth = SportKidTheme.spacing.xs,
            placeholderColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            placeholderContentColor = MaterialTheme.colorScheme.outline,
            shadowElevation = SkElevation.level2,
        )
    }
}
