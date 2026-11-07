package com.invex.sportkid.core.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.theme.AppPalette
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.metric

/** Catálogo visual de tokens, para validar contra Stitch / Theme Builder (README §8). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TokensCatalog() {
    val c = MaterialTheme.colorScheme
    val s = SportKidTheme.sportColors
    val sp = SportKidTheme.spacing
    Column(
        Modifier
            .background(c.surface)
            .verticalScroll(rememberScrollState())
            .padding(sp.screenMargin),
        verticalArrangement = Arrangement.spacedBy(sp.sm),
    ) {
        Section("ROLES M3")
        Swatches(
            "primary" to c.primary, "primaryContainer" to c.primaryContainer,
            "secondary" to c.secondary, "secondaryContainer" to c.secondaryContainer,
            "tertiary" to c.tertiary, "tertiaryContainer" to c.tertiaryContainer,
            "error" to c.error, "surface" to c.surface,
            "surfaceContainerLowest" to c.surfaceContainerLowest, "surfaceContainer" to c.surfaceContainer,
            "outline" to c.outline, "outlineVariant" to c.outlineVariant,
        )
        Section("DEPORTE · MEDALLA · ESTADO")
        Swatches(
            "tkd" to s.tkd, "tkdContainer" to s.tkdContainer,
            "swimming" to s.swimming, "swimmingContainer" to s.swimmingContainer,
            "athletics" to s.athletics, "athleticsContainer" to s.athleticsContainer,
            "gold" to s.medalGold, "silver" to s.medalSilver, "bronze" to s.medalBronze,
            "success" to s.success, "successContainer" to s.successContainer,
            "warning" to s.warning, "warningContainer" to s.warningContainer,
        )
        Section("HIJOS")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(sp.sm)) {
            s.athlete.all.forEach { Box(it, 24) }
        }
        Section("TIPOGRAFÍA")
        val t = MaterialTheme.typography
        Text("$1,250 MXN", style = t.displaySmall, color = c.onSurface)
        Text("Mateo", style = t.headlineSmall, color = c.onSurface)
        Text("Torneo estatal", style = t.titleLarge, color = c.onSurface)
        Text("Sáb 14 nov · 10:00", style = t.titleMedium, color = c.onSurface)
        Text("Descripción del evento", style = t.bodyMedium, color = c.onSurfaceVariant)
        Text("PRÓXIMOS", style = t.labelSmall, color = c.onSurfaceVariant)
        Text("00:41.23", style = t.metric, color = c.onSurface)
    }
}

@Composable
private fun Section(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = SportKidTheme.spacing.md),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Swatches(vararg colors: Pair<String, Color>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
    ) {
        colors.forEach { (name, color) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(color, 48)
                Text(name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun Box(color: Color, size: Int) {
    androidx.compose.foundation.layout.Box(
        Modifier
            .size(size.dp)
            .background(color, MaterialTheme.shapes.small)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small),
    )
}

@SkPreviews
@Composable
private fun TokensOceanPreview() = SportKidTheme { TokensCatalog() }

@Preview(name = "Dojang (MaterialKolor)", showBackground = true)
@Composable
private fun TokensDojangPreview() = SportKidTheme(palette = AppPalette.Dojang) { TokensCatalog() }
