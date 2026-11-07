package com.invex.sportkid.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Sport

/** Etiqueta de deporte activo: icono y nombre sobre el contenedor del deporte (Figma: "Active Sports Tags"). */
@Composable
fun SportTag(sport: Sport, modifier: Modifier = Modifier) {
    val colors = sport.colors()
    SkPill(
        text = stringResource(sport.labelRes),
        modifier = modifier,
        containerColor = colors.container,
        contentColor = colors.onContainer,
        textStyle = MaterialTheme.typography.labelMedium,
        spacing = 6.dp,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        leading = {
            Icon(painterResource(sport.iconRes), contentDescription = null, modifier = Modifier.size(14.dp), tint = colors.content)
        },
    )
}

@SkPreviews
@Composable
private fun SportTagPreview() = SportKidTheme {
    Row(Modifier.padding(16.dp)) {
        Sport.entries.forEach { SportTag(it, Modifier.padding(end = 8.dp)) }
    }
}
