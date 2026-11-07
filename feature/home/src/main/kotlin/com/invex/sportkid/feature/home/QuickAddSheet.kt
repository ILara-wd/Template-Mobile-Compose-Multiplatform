package com.invex.sportkid.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.R as DsR
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.labelRes

/** Tipos de registro que ofrece O4. */
enum class QuickAddAction { Training, BeltExam, TkdTournament, SwimMeet, Race }

/**
 * O4. Sheet de registro rápido (FAB "Nuevo evento"): grid 2×2 de tiles, la carrera a todo el
 * ancho y una nota sobre el calendario familiar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onSelect: (QuickAddAction) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
        dragHandle = { DragHandle() },
    ) {
        QuickAddContent(onClose = onDismiss, onSelect = onSelect)
    }
}

@Composable
private fun DragHandle() {
    Box(
        Modifier
            .padding(top = SportKidTheme.spacing.md, bottom = SportKidTheme.spacing.xs)
            .size(width = 32.dp, height = 4.dp)
            .background(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.full),
    )
}

@Composable
internal fun QuickAddContent(onClose: () -> Unit, onSelect: (QuickAddAction) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, bottom = SportKidTheme.spacing.xxl),
    ) {
        Row(Modifier.padding(top = SportKidTheme.spacing.sm, bottom = SportKidTheme.spacing.md), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
                SkPill(
                    stringResource(R.string.quick_add_overline),
                    icon = painterResource(R.drawable.ic_quick_add_bolt),
                    overline = true,
                    spacing = 6.dp,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                )
                Text(
                    stringResource(R.string.quick_add_title),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                )
            }
            IconButton(onClick = onClose) {
                Icon(painterResource(DsR.drawable.sk_ic_close), stringResource(R.string.quick_add_close), tint = colors.onSurfaceVariant)
            }
        }

        val tkd = Sport.TAEKWONDO.colors()
        val swim = Sport.SWIMMING.colors()
        val gap = Arrangement.spacedBy(SportKidTheme.spacing.sm)
        Column(Modifier.padding(top = SportKidTheme.spacing.xs), verticalArrangement = gap) {
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = gap) {
                ActionTile(
                    icon = R.drawable.ic_quick_add_training,
                    iconContainer = colors.primary,
                    tag = stringResource(R.string.quick_add_training_tag),
                    tagContainer = colors.secondaryContainer,
                    tagContent = colors.onSecondaryContainer,
                    title = stringResource(R.string.quick_add_training),
                    description = stringResource(R.string.quick_add_training_desc),
                    onClick = { onSelect(QuickAddAction.Training) },
                    modifier = Modifier.weight(1f),
                )
                ActionTile(
                    icon = R.drawable.ic_quick_add_belt_exam,
                    iconContainer = tkd.content,
                    tag = stringResource(Sport.TAEKWONDO.labelRes),
                    tagContainer = tkd.container,
                    tagContent = tkd.onContainer,
                    title = stringResource(R.string.quick_add_belt_exam),
                    description = stringResource(R.string.quick_add_belt_exam_desc),
                    onClick = { onSelect(QuickAddAction.BeltExam) },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = gap) {
                ActionTile(
                    icon = R.drawable.ic_quick_add_tournament,
                    iconContainer = tkd.content,
                    tag = stringResource(Sport.TAEKWONDO.labelRes),
                    tagContainer = tkd.container,
                    tagContent = tkd.onContainer,
                    title = stringResource(R.string.quick_add_tournament),
                    description = stringResource(R.string.quick_add_tournament_desc),
                    onClick = { onSelect(QuickAddAction.TkdTournament) },
                    modifier = Modifier.weight(1f),
                )
                ActionTile(
                    icon = Sport.SWIMMING.iconRes,
                    iconContainer = swim.content,
                    tag = stringResource(Sport.SWIMMING.labelRes),
                    tagContainer = swim.container,
                    tagContent = swim.onContainer,
                    title = stringResource(R.string.quick_add_swim_meet),
                    description = stringResource(R.string.quick_add_swim_meet_desc),
                    onClick = { onSelect(QuickAddAction.SwimMeet) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(SportKidTheme.spacing.lg))
        RaceTile(onClick = { onSelect(QuickAddAction.Race) })
        Spacer(Modifier.height(SportKidTheme.spacing.lg))
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainer, MaterialTheme.shapes.medium)
                .padding(SportKidTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.onSurfaceVariant)
            Text(stringResource(R.string.quick_add_tip), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

/** Icono del tile: cuadrado redondeado de 48dp en el color de la acción. */
@Composable
private fun IconTile(@DrawableRes icon: Int, container: Color) {
    Box(
        Modifier.size(48.dp).background(container, MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.surfaceContainerLowest,
        )
    }
}

@Composable
private fun Chevron() {
    Icon(painterResource(DsR.drawable.sk_ic_chevron_right), contentDescription = null, tint = MaterialTheme.colorScheme.outline)
}

@Composable
private fun ActionTile(
    @DrawableRes icon: Int,
    iconContainer: Color,
    tag: String,
    tagContainer: Color,
    tagContent: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceContainerLowest,
        shadowElevation = SkElevation.card,
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconTile(icon, iconContainer)
                Spacer(Modifier.weight(1f))
                Chevron()
            }
            Column(Modifier.padding(top = 17.dp), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
                SkPill(
                    tag,
                    containerColor = tagContainer,
                    contentColor = tagContent,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                )
                Text(title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RaceTile(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val athletics = Sport.ATHLETICS.colors()
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceContainerLowest,
        shadowElevation = SkElevation.card,
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTile(Sport.ATHLETICS.iconRes, athletics.content)
            Spacer(Modifier.width(SportKidTheme.spacing.md))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.quick_add_race), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    Spacer(Modifier.width(SportKidTheme.spacing.sm))
                    SkPill(
                        stringResource(Sport.ATHLETICS.labelRes),
                        containerColor = athletics.container,
                        contentColor = athletics.onContainer,
                        contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm),
                    )
                }
                Text(stringResource(R.string.quick_add_race_desc), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Chevron()
        }
    }
}

@SkPreviews
@Composable
private fun QuickAddContentPreview() = SportKidTheme {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        QuickAddContent(onClose = {}, onSelect = {})
    }
}
