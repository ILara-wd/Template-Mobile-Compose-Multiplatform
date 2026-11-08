package com.invex.sportkid.feature.sharing

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkCard
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkHelpButton
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkLinearProgress
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkProfileButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Belt
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.compactLabel
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.labelRes
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * P18, vista previa y descarga (O25): confirmación del código, card del deportista con sus
 * deportes, logros y el permiso que recibirás, y el avance de la descarga por bloque.
 * "Ir al perfil" queda disponible durante la descarga; termina en segundo plano.
 */
@Composable
fun JoinPreviewScreen(
    preview: JoinPreview,
    steps: List<SyncStep>,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    onHelp: () -> Unit = {},
    onProfile: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.preview_title), onBack = onBack) {
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
                .padding(bottom = SportKidTheme.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg),
        ) {
            SuccessBanner()
            AthletePreviewCard(preview, today)
            SyncCard(preview.athlete.firstName, steps)
            Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SkButton(
                    stringResource(R.string.preview_open_profile, preview.athlete.firstName),
                    onClick = onOpenProfile,
                    modifier = Modifier.fillMaxWidth(),
                    size = SkButtonSize.Medium,
                    trailingIcon = painterResource(R.drawable.ic_join_arrow_forward),
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = SportKidTheme.spacing.sm),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.ic_join_cloud_done), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.preview_footer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** Banner "Detección exitosa" que confirma el código validado. */
@Composable
private fun SuccessBanner() {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = colors.secondaryContainer, shadowElevation = SkElevation.level1) {
        Row(Modifier.padding(SportKidTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            SkIconCircle(
                icon = painterResource(R.drawable.ic_join_verified),
                contentDescription = null,
                containerColor = colors.primaryContainer,
                iconTint = colors.primary,
            )
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Column {
                Text(
                    stringResource(R.string.preview_banner_overline).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.3.sp),
                    color = colors.primary,
                )
                Text(stringResource(R.string.preview_banner_title), style = MaterialTheme.typography.titleSmall, color = colors.secondary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
/** Card del deportista al que te unes: avatar, edad, tutor, deportes, logros y el permiso que recibirás. */
@Composable
private fun AthletePreviewCard(preview: JoinPreview, today: LocalDate) {
    val colors = MaterialTheme.colorScheme
    val athlete = preview.athlete
    SkCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        accentColor = colors.primary,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
            Row(Modifier.padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                SkAvatar(
                    photo = athlete.photoRes?.let { painterResource(it) },
                    name = athlete.fullName,
                    size = 80.dp,
                    ringColor = colors.primaryContainer,
                    ringWidth = 4.dp,
                ) {
                    athlete.sports.firstOrNull()?.let { sport ->
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .offset(4.dp, 4.dp)
                                .size(28.dp)
                                .background(colors.primary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(painterResource(sport.iconRes), contentDescription = null, modifier = Modifier.size(14.dp), tint = colors.onPrimary)
                        }
                    }
                }
                Spacer(Modifier.width(SportKidTheme.spacing.lg))
                Column(Modifier.weight(1f)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.Center) {
                        Text(
                            athlete.fullName,
                            modifier = Modifier.align(Alignment.CenterVertically).semantics { heading() },
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.onSurface,
                        )
                        SkPill(
                            stringResource(R.string.preview_age, athlete.birthDate.ageOn(today)),
                            modifier = Modifier.align(Alignment.CenterVertically),
                            containerColor = colors.surfaceContainerHigh,
                            contentColor = colors.onSurfaceVariant,
                            contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                            textStyle = MaterialTheme.typography.labelMedium,
                        )
                    }
                    IconLine(painterResource(R.drawable.ic_join_cake), athlete.birthDate.compactLabel(), MaterialTheme.typography.bodySmall, colors.onSurfaceVariant, Modifier.padding(top = 2.dp))
                    IconLine(
                        painterResource(R.drawable.ic_join_tutor),
                        stringResource(R.string.preview_tutor, preview.tutorName, preview.tutorRelation),
                        MaterialTheme.typography.labelSmall,
                        colors.secondary,
                        Modifier.padding(top = 4.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                preview.sports.forEach { SportRow(it) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                MetricTile(
                    icon = painterResource(R.drawable.ic_share_medal),
                    containerColor = colors.tertiaryContainer,
                    iconTint = colors.tertiary,
                    value = pluralStringResource(R.plurals.preview_medals, preview.medals, preview.medals),
                    label = stringResource(R.string.preview_medals_label),
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    icon = painterResource(R.drawable.ic_join_event),
                    containerColor = colors.primaryContainer,
                    iconTint = colors.primary,
                    value = pluralStringResource(R.plurals.preview_events, preview.events, preview.events),
                    label = stringResource(R.string.preview_events_label),
                    modifier = Modifier.weight(1f),
                )
            }
            val role = stringResource(preview.role.labelRes)
            val permission = stringResource(R.string.preview_permission, role)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainer, MaterialTheme.shapes.small)
                    .padding(horizontal = SportKidTheme.spacing.md, vertical = SportKidTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_join_family), contentDescription = null, tint = colors.primary)
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                Text(
                    buildAnnotatedString {
                        val start = permission.indexOf(role)
                        append(permission)
                        if (start >= 0) addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, start + role.length)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

/** Icono pequeño con una línea de texto (fecha de nacimiento, tutor). */
@Composable
private fun IconLine(icon: Painter, text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(Modifier.width(4.dp))
        Text(text, style = style, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Pill del deporte: punto del color del deporte, nombre y avance ("· Cinta verde (6° Kup)"). */
@Composable
private fun SportRow(snapshot: SportSnapshot) {
    val colors = MaterialTheme.colorScheme
    val detail = when {
        snapshot.belt != null -> stringResource(snapshot.belt.labelRes())
        snapshot.stylesMastered != null -> stringResource(R.string.preview_styles, snapshot.stylesMastered, snapshot.stylesTotal)
        else -> null
    }
    Row(
        Modifier
            .height(28.dp)
            .background(colors.surfaceContainer, CircleShape)
            .padding(horizontal = SportKidTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(10.dp).background(snapshot.sport.colors().content, CircleShape))
        Text(stringResource(snapshot.sport.labelRes), style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
        if (detail != null) {
            Text(stringResource(R.string.preview_detail, detail), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
    }
}

/** Métrica con icono en círculo, valor y etiqueta ("4 medallas · Logros acumulados"). */
@Composable
private fun MetricTile(icon: Painter, containerColor: Color, iconTint: Color, value: String, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
            .padding(SportKidTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkIconCircle(icon = icon, contentDescription = null, containerColor = containerColor, iconTint = iconTint)
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        Column {
            Text(value, style = MaterialTheme.typography.titleMedium, color = colors.onSurface, maxLines = 1)
            Text(label, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Avance de la descarga: encabezado con porcentaje, barra y un renglón por bloque. */
@Composable
private fun SyncCard(firstName: String, steps: List<SyncStep>) {
    val colors = MaterialTheme.colorScheme
    val progress by animateFloatAsState(steps.progress, label = "syncProgress")
    val done = steps.all { it.isDone }
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = colors.surfaceContainerLowest, shadowElevation = SkElevation.card) {
        Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
            Row(Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }, verticalAlignment = Alignment.CenterVertically) {
                SyncIcon(spinning = !done)
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(if (done) R.string.preview_sync_done_title else R.string.preview_sync_title, firstName),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface,
                    )
                    Text(
                        stringResource(if (done) R.string.preview_sync_done_subtitle else R.string.preview_sync_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                Text(
                    stringResource(R.string.preview_sync_percent, (steps.progress * 100).roundToInt()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary,
                )
            }
            SkLinearProgress(progress = progress, height = 10.dp, trackColor = colors.surfaceContainerHigh)
            Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                steps.forEach { StepRow(it) }
            }
        }
    }
}

/** Icono de sincronización que gira mientras descarga y cambia a check al terminar. */
@Composable
private fun SyncIcon(spinning: Boolean) {
    val rotation = if (spinning) {
        val transition = rememberInfiniteTransition(label = "syncIcon")
        val angle by transition.animateFloat(
            initialValue = 0f,
            targetValue = -360f,
            animationSpec = infiniteRepeatable(tween(1_400, easing = LinearEasing), RepeatMode.Restart),
            label = "syncIconAngle",
        )
        angle
    } else {
        0f
    }
    val colors = MaterialTheme.colorScheme
    Box(Modifier.size(32.dp).background(colors.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
        Icon(
            painterResource(if (spinning) R.drawable.ic_join_sync else R.drawable.ic_join_check),
            contentDescription = null,
            modifier = Modifier.rotate(rotation),
            tint = colors.primary,
        )
    }
}

/** Renglón de un bloque de la descarga: sincronizado, en curso ("8 de 12…") o en espera. */
@Composable
private fun StepRow(step: SyncStep) {
    val colors = MaterialTheme.colorScheme
    val label = stringResource(
        when (step.section) {
            SyncSection.PROFILE -> R.string.preview_step_profile
            SyncSection.EVENTS -> R.string.preview_step_events
            SyncSection.MEDIA -> R.string.preview_step_media
            SyncSection.REMINDERS -> R.string.preview_step_reminders
        },
    )
    val inProgress = !step.isDone && !step.isPending
    Row(
        Modifier
            .fillMaxWidth()
            .alpha(if (step.isPending) 0.6f else 1f)
            .background(if (inProgress) colors.tertiaryContainer.copy(alpha = 0.4f) else colors.surfaceContainerLow, MaterialTheme.shapes.small)
            .padding(horizontal = SportKidTheme.spacing.sm, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (circle, icon, tint) = when {
            step.isDone -> Triple(colors.primaryContainer, R.drawable.ic_join_check, colors.primary)
            inProgress -> Triple(colors.tertiaryContainer, R.drawable.ic_join_hourglass, colors.tertiary)
            else -> Triple(colors.surfaceContainerHigh, R.drawable.ic_join_more, colors.outline)
        }
        Box(Modifier.size(24.dp).background(circle, CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = null, tint = tint)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = if (inProgress) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (step.isPending) colors.onSurfaceVariant else colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(SportKidTheme.spacing.xs))
        when {
            step.isDone -> SkPill(
                stringResource(R.string.preview_step_synced),
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
                contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
            )
            inProgress -> SkPill(
                stringResource(R.string.preview_step_progress, step.done, step.total),
                containerColor = colors.tertiary,
                contentColor = colors.tertiaryContainer,
                contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
            )
            else -> SkPill(
                stringResource(R.string.preview_step_waiting),
                containerColor = colors.surfaceContainer,
                contentColor = colors.outline,
                contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
            )
        }
    }
}

@SkPreviews
@Composable
private fun JoinPreviewScreenPreview() = SportKidTheme {
    JoinPreviewScreen(
        preview = SharingSamples.preview(SampleShareAthlete, belt = Belt.GREEN, stylesMastered = 3, medals = 4),
        steps = SharingSamples.initialSync,
        onBack = {},
        onOpenProfile = {},
        today = LocalDate.of(2026, 11, 7),
    )
}
