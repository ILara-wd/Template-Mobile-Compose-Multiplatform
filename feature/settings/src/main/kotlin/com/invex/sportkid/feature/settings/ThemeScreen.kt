package com.invex.sportkid.feature.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkHelpButton
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkProfileButton
import com.invex.sportkid.core.designsystem.component.SkSegmentedControl
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.AppPalette
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.ThemeMode
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.labelRes

/** Nombre visible del tema. */
@get:StringRes
val AppPalette.labelRes: Int
    get() = when (this) {
        AppPalette.Automatic -> R.string.palette_automatic
        AppPalette.Ocean -> R.string.palette_ocean
        AppPalette.Dojang -> R.string.palette_dojang
        AppPalette.Alberca -> R.string.palette_alberca
        AppPalette.Pista -> R.string.palette_pista
        AppPalette.Bosque -> R.string.palette_bosque
    }

/** Matiz del tema ("Granate marcial"). */
@get:StringRes
val AppPalette.detailRes: Int
    get() = when (this) {
        AppPalette.Automatic -> R.string.palette_automatic_detail
        AppPalette.Ocean -> R.string.palette_ocean_detail
        AppPalette.Dojang -> R.string.palette_dojang_detail
        AppPalette.Alberca -> R.string.palette_alberca_detail
        AppPalette.Pista -> R.string.palette_pista_detail
        AppPalette.Bosque -> R.string.palette_bosque_detail
    }

/** Icono del FAB del mini mock: el deporte que inspira cada paleta. */
@DrawableRes
private fun AppPalette.fabIcon(): Int? = when (this) {
    AppPalette.Automatic -> null
    AppPalette.Ocean -> R.drawable.ic_theme_add
    AppPalette.Dojang -> Sport.TAEKWONDO.iconRes
    AppPalette.Alberca -> Sport.SWIMMING.iconRes
    AppPalette.Pista -> Sport.ATHLETICS.iconRes
    AppPalette.Bosque -> R.drawable.ic_theme_forest
}

/** Degradado que representa "Material You" (colores del fondo de pantalla). */
private val DynamicBrush = Brush.linearGradient(listOf(Color(0xFF9FCAFF), Color(0xFFDFBFE6), Color(0xFFBDE2BF)))

/**
 * P21. Tema y apariencia (plantilla T5): modo Sistema / Claro / Oscuro y grid de paletas (O28)
 * con un mini mock pintado con el esquema real de cada una (M26). La selección se aplica con
 * el botón; los colores de deporte e hijos no cambian (README §1.2).
 */
@Composable
fun ThemeScreen(
    palette: AppPalette,
    mode: ThemeMode,
    onApply: (AppPalette, ThemeMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    palettes: List<AppPalette> = AppPalette.available,
    onHelp: () -> Unit = {},
    onProfile: () -> Unit = {},
) {
    var pendingPalette by rememberSaveable(palette) { mutableStateOf(palette) }
    var pendingMode by rememberSaveable(mode) { mutableStateOf(mode) }
    val applied = pendingPalette == palette && pendingMode == mode
    val colors = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier,
        containerColor = colors.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.theme_title), onBack = onBack) {
                SkHelpButton(onClick = onHelp)
                SkProfileButton(onClick = onProfile)
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = SportKidTheme.spacing.screenMargin)
                .padding(top = SportKidTheme.spacing.lg, bottom = SportKidTheme.spacing.xxl),
        ) {
            Row {
                SkIconCircle(
                    icon = painterResource(R.drawable.ic_theme_palette),
                    contentDescription = null,
                    containerColor = colors.secondaryContainer,
                    iconTint = colors.secondary,
                    shadowElevation = SkElevation.level1,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                Column {
                    Text(stringResource(R.string.theme_intro_title), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    Text(stringResource(R.string.theme_intro_body), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
            Text(
                stringResource(R.string.theme_mode_label).uppercase(),
                modifier = Modifier.padding(top = SportKidTheme.spacing.xl, bottom = 4.dp),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.6.sp),
                color = colors.onSurfaceVariant,
            )
            SkSegmentedControl(
                labels = ThemeMode.entries.map { stringResource(it.labelRes) },
                selectedIndex = pendingMode.ordinal,
                onSelect = { pendingMode = ThemeMode.entries[it] },
                icons = ThemeMode.entries.map { painterResource(it.iconRes) },
            )
            Row(Modifier.padding(top = SportKidTheme.spacing.xl, bottom = SportKidTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.theme_palette_title),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                    )
                    Text(stringResource(R.string.theme_palette_body), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                SkPill(
                    pluralStringResource(R.plurals.theme_palette_count, palettes.size, palettes.size),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
                palettes.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
                        row.forEach { item ->
                            PaletteCard(
                                palette = item,
                                mode = pendingMode,
                                selected = item == pendingPalette,
                                onClick = { pendingPalette = item },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            FixedColorsBanner(Modifier.padding(top = SportKidTheme.spacing.xl))
            SkButton(
                stringResource(if (applied) R.string.theme_applied else R.string.theme_apply),
                onClick = { if (applied) onBack() else onApply(pendingPalette, pendingMode) },
                modifier = Modifier
                    .padding(top = SportKidTheme.spacing.xl)
                    .fillMaxWidth(),
                leadingIcon = painterResource(R.drawable.ic_theme_applied).takeIf { applied },
                size = SkButtonSize.Medium,
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = SportKidTheme.spacing.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_theme_sync), contentDescription = null, modifier = Modifier.size(10.dp), tint = colors.primary)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.theme_sync_note), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

@get:StringRes
private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.System -> R.string.mode_system
        ThemeMode.Light -> R.string.mode_light
        ThemeMode.Dark -> R.string.mode_dark
    }

@get:DrawableRes
private val ThemeMode.iconRes: Int
    get() = when (this) {
        ThemeMode.System -> R.drawable.ic_theme_mode_system
        ThemeMode.Light -> R.drawable.ic_theme_mode_light
        ThemeMode.Dark -> R.drawable.ic_theme_mode_dark
    }

/** M26. Tarjeta de un tema: mini mock con su esquema, nombre, matiz y check si está elegido. */
@Composable
private fun PaletteCard(palette: AppPalette, mode: ThemeMode, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceContainerLowest,
        shadowElevation = if (selected) SkElevation.level3 else SkElevation.card,
    ) {
        Box {
            Column(Modifier.padding(SportKidTheme.spacing.md)) {
                // El mock usa el esquema real de la paleta (no el tema actual de la app).
                SportKidTheme(palette = palette, themeMode = mode) {
                    MiniPreview(palette)
                }
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(palette.labelRes),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold),
                        color = if (selected) colors.primary else colors.onSurface,
                    )
                    SportKidTheme(palette = palette, themeMode = mode) {
                        Swatch(palette)
                    }
                }
                Text(
                    stringResource(palette.detailRes),
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) colors.primary else colors.onSurfaceVariant,
                )
            }
            when {
                selected -> SkIconCircle(
                    icon = painterResource(R.drawable.ic_theme_check),
                    contentDescription = null,
                    size = 20.dp,
                    containerColor = colors.primary,
                    iconTint = colors.onPrimary,
                    shadowElevation = SkElevation.level1,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(SportKidTheme.spacing.sm),
                )
                palette == AppPalette.Automatic -> SkPill(
                    stringResource(R.string.theme_requires_android12),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(SportKidTheme.spacing.sm),
                    containerColor = colors.surfaceContainerHighest,
                    contentColor = colors.onSurfaceVariant,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun Swatch(palette: AppPalette) {
    val modifier = Modifier
        .size(16.dp)
        .then(
            if (palette == AppPalette.Automatic) {
                Modifier.background(DynamicBrush, CircleShape)
            } else {
                Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
            },
        )
    Box(modifier)
}

/** Mini mock (top bar, card y FAB) con el esquema de la paleta que lo envuelve. */
@Composable
private fun MiniPreview(palette: AppPalette) {
    val colors = MaterialTheme.colorScheme
    val dynamic = palette == AppPalette.Automatic
    Column(
        Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.small)
            .padding(6.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(if (dynamic) DynamicBrush else Brush.linearGradient(listOf(colors.primary, colors.primary)), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(6.dp).background(Color.White.copy(alpha = if (dynamic) 0.8f else 1f), CircleShape))
            Spacer(Modifier.weight(1f))
            if (!dynamic) Box(Modifier.width(20.dp).height(4.dp).background(Color.White.copy(alpha = 0.6f), CircleShape))
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerLowest, RoundedCornerShape(4.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.75f)
                    .height(6.dp)
                    .background(if (dynamic) DynamicBrush else Brush.linearGradient(listOf(colors.primaryContainer, colors.primaryContainer)), RoundedCornerShape(4.dp)),
            )
            Box(Modifier.fillMaxWidth(0.5f).height(4.dp).background(colors.surfaceContainerHighest, RoundedCornerShape(4.dp)))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                Modifier
                    .size(16.dp)
                    .background(if (dynamic) DynamicBrush else Brush.linearGradient(listOf(colors.primary, colors.primary)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                val icon = palette.fabIcon()
                if (icon == null) {
                    Box(Modifier.size(6.dp).background(Color.White, CircleShape))
                } else {
                    Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(9.dp), tint = colors.onPrimary)
                }
            }
        }
    }
}

/** Recordatorio de que los colores de deporte e hijos son fijos, con su muestrario. */
@Composable
private fun FixedColorsBanner(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val sports = Sport.entries
    fun hex(color: Color) = "#%06X".format(color.toArgb() and 0xFFFFFF)
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = colors.surfaceContainerLow, shadowElevation = SkElevation.card) {
        Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
            Row {
                SkIconCircle(
                    icon = painterResource(R.drawable.ic_theme_info),
                    contentDescription = null,
                    size = 36.dp,
                    containerColor = colors.secondaryContainer,
                    iconTint = colors.onSecondaryContainer,
                )
                Spacer(Modifier.width(SportKidTheme.spacing.md))
                Column {
                    Text(stringResource(R.string.theme_fixed_title), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    Text(
                        stringResource(
                            R.string.theme_fixed_body,
                            hex(Sport.TAEKWONDO.colors().content),
                            hex(Sport.SWIMMING.colors().content),
                            hex(Sport.ATHLETICS.colors().content),
                        ),
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerLowest.copy(alpha = 0.8f), MaterialTheme.shapes.small)
                    .padding(horizontal = SportKidTheme.spacing.md, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                sports.forEach { sport ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(12.dp).background(sport.colors().content, CircleShape))
                        Text(stringResource(sport.labelRes), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@SkPreviews
@Composable
private fun ThemeScreenPreview() = SportKidTheme {
    ThemeScreen(palette = AppPalette.Ocean, mode = ThemeMode.Light, onApply = { _, _ -> }, onBack = {}, palettes = AppPalette.entries)
}
