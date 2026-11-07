package com.invex.sportkid.feature.athletics

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkCard
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkTextButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.MedalResult
import com.invex.sportkid.core.model.PaymentStatus
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.AthleteSportHeader
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.labelRes
import com.invex.sportkid.core.ui.shortLabelWithYear
import java.time.LocalDate
import com.invex.sportkid.core.ui.R as UiR

/**
 * P14. Atletismo de un deportista: resumen con categoría y medallas, "Registrar carrera",
 * mejores marcas (O21), galería de medallas (O22) y carreras disputadas.
 */
@Composable
fun AthleticsScreen(
    athlete: AthleticsAthlete,
    onBack: () -> Unit,
    onNewRace: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    bestMarks: List<BestMark> = AthleticsSamples.bestMarks,
    gallery: List<MedalPhoto> = AthleticsSamples.gallery,
    nextGoal: String? = AthleticsSamples.NEXT_GOAL,
    races: List<Race> = AthleticsSamples.races,
    onSelectSport: (Sport) -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val golds = races.count { it.result == MedalResult.GOLD }
    val silvers = races.count { it.result == MedalResult.SILVER }
    Scaffold(
        modifier = modifier,
        containerColor = colors.surface,
        topBar = { SkDetailTopBar(title = stringResource(R.string.ath_screen_title), subtitle = athlete.fullName, onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewRace, containerColor = colors.tertiary, contentColor = colors.onTertiary, shape = MaterialTheme.shapes.large) {
                Icon(painterResource(R.drawable.ic_ath_speed), contentDescription = stringResource(R.string.ath_quick_log))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(top = SportKidTheme.spacing.sm, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xl),
        ) {
            item(key = "header") {
                Column(Modifier.padding(horizontal = SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
                    AthleteSportHeader(
                        name = athlete.fullName,
                        photo = athlete.photoRes?.let { painterResource(it) },
                        ringColor = athlete.color(),
                        age = athlete.birthDate.ageOn(today),
                        subtitle = athlete.club,
                        subtitleIcon = painterResource(Sport.ATHLETICS.iconRes),
                        sport = Sport.ATHLETICS,
                        sports = athlete.sports,
                        onSelectSport = onSelectSport,
                    ) {
                        SkPill(
                            stringResource(R.string.ath_category, athlete.category),
                            icon = painterResource(R.drawable.ic_ath_category),
                        )
                        SkPill(
                            stringResource(
                                R.string.ath_medals_summary,
                                pluralStringResource(R.plurals.ath_golds, golds, golds),
                                pluralStringResource(R.plurals.ath_silvers, silvers, silvers),
                            ),
                            containerColor = colors.tertiaryContainer,
                            contentColor = colors.onTertiaryContainer,
                            icon = painterResource(UiR.drawable.ic_medal),
                        )
                    }
                    SkButton(
                        stringResource(R.string.ath_new_race),
                        onClick = onNewRace,
                        leadingIcon = painterResource(R.drawable.ic_ath_add),
                        size = SkButtonSize.Medium,
                        containerColor = colors.tertiary,
                        contentColor = colors.onTertiary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item(key = "marks") { BestMarks(bestMarks) }
            item(key = "gallery") { MedalGallery(gallery, nextGoal, Modifier.padding(horizontal = SportKidTheme.spacing.lg)) }
            item(key = "races") { RaceList(races, onNewRace, Modifier.padding(horizontal = SportKidTheme.spacing.lg)) }
        }
    }
}

@Composable
private fun SectionTitle(icon: Int, title: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), contentDescription = null, tint = SportKidTheme.sportColors.athletics)
        Spacer(Modifier.width(6.dp))
        Text(title, modifier = Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        trailing()
    }
}

/** Círculo de medalla (oro/plata/bronce) sobre fondo tenue. */
@Composable
private fun MedalDot(medal: MedalResult, size: Int = 28, modifier: Modifier = Modifier) {
    val c = medal.colors()
    Box(modifier.size(size.dp).background(c.container, CircleShape).border(1.dp, c.accent, CircleShape), contentAlignment = Alignment.Center) {
        Icon(painterResource(UiR.drawable.ic_medal), contentDescription = stringResource(medal.labelRes()), modifier = Modifier.size((size * 0.55f).dp), tint = c.accent)
    }
}

/** O21. Mejores marcas por distancia, deslizables. La marca récord va resaltada. */
@Composable
private fun BestMarks(marks: List<BestMark>) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionTitle(R.drawable.ic_ath_timer, stringResource(R.string.ath_best_marks), Modifier.padding(horizontal = SportKidTheme.spacing.lg)) {
            Text(stringResource(R.string.ath_best_marks_hint), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.lg, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        ) {
            items(marks, key = { "${it.distance}-${it.type}" }) { BestMarkCard(it) }
        }
    }
}

@Composable
private fun BestMarkCard(mark: BestMark) {
    val colors = MaterialTheme.colorScheme
    SkCard(
        modifier = Modifier.width(190.dp),
        accentColor = if (mark.isRecord) colors.tertiary else SportKidTheme.sportColors.athletics,
        accentWidth = if (mark.isRecord) 6.dp else 4.dp,
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(Modifier.fillMaxWidth().padding(start = if (mark.isRecord) 6.dp else 4.dp).background(if (mark.isRecord) colors.tertiaryContainer.copy(alpha = 0.4f) else Color.Transparent)) {
            Column(Modifier.padding(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = SportKidTheme.spacing.lg, bottom = SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(distanceLabel(mark.distance), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                    SkPill(
                        if (mark.isRecord) stringResource(R.string.ath_record) else mark.type.label(),
                        containerColor = if (mark.isRecord) colors.tertiary else colors.surfaceContainerHigh,
                        contentColor = if (mark.isRecord) colors.onTertiary else colors.onSurfaceVariant,
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(mark.time.short(), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = colors.tertiary)
                    Spacer(Modifier.width(SportKidTheme.spacing.xs))
                    Text(stringResource(R.string.ath_minutes), modifier = Modifier.padding(bottom = 4.dp), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(mark.date.shortLabelWithYear(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    mark.medal?.let { MedalDot(it, size = 24) }
                }
            }
        }
    }
}

/** O22. Galería de medallas en 3 columnas con la próxima meta al final. */
@Composable
private fun MedalGallery(photos: List<MedalPhoto>, nextGoal: String?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val columns = 3
    // `null` = mosaico de "Próxima meta", que ocupa las columnas libres de la última fila.
    val tiles: List<MedalPhoto?> = if (nextGoal != null) photos + null else photos
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionTitle(R.drawable.ic_ath_gallery, stringResource(R.string.ath_gallery)) {
            Text(pluralStringResource(R.plurals.ath_gallery_count, photos.size, photos.size), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val gap = SportKidTheme.spacing.sm
            val tile = (maxWidth - gap * (columns - 1)) / columns
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                tiles.chunked(columns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        row.forEach { photo ->
                            if (photo != null) {
                                MedalTile(photo, Modifier.size(tile))
                            } else {
                                val span = columns - row.size + 1
                                NextGoalTile(nextGoal.orEmpty(), Modifier.size(tile * span + gap * (span - 1), tile))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedalTile(photo: MedalPhoto, modifier: Modifier = Modifier) {
    Box(modifier.clip(MaterialTheme.shapes.medium)) {
        Image(painterResource(photo.photoRes), contentDescription = photo.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.4f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.7f))))
        MedalDot(photo.medal, size = 22, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
        Column(Modifier.align(Alignment.BottomStart).padding(6.dp)) {
            Text(photo.title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${photo.year}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun NextGoalTile(goal: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Sport.ATHLETICS.colors().container)
            .padding(SportKidTheme.spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(R.drawable.ic_ath_goal), contentDescription = null, tint = Sport.ATHLETICS.colors().onContainer)
        Text(stringResource(R.string.ath_next_goal), modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        Text(goal, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Sport.ATHLETICS.colors().onContainer, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RaceList(races: List<Race>, onNewRace: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionTitle(R.drawable.ic_ath_flag, stringResource(R.string.ath_races)) {
            SkTextButton(stringResource(R.string.ath_races_add), onClick = onNewRace, color = colors.tertiary, textStyle = MaterialTheme.typography.labelSmall)
        }
        races.forEach { RaceCard(it) }
    }
}

@Composable
private fun RaceCard(race: Race) {
    val colors = MaterialTheme.colorScheme
    SkCard(accentColor = SportKidTheme.sportColors.athletics) {
        Column(Modifier.padding(start = SportKidTheme.spacing.xs), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(race.name, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(UiR.drawable.ic_form_date), contentDescription = null, modifier = Modifier.size(12.dp), tint = colors.onSurfaceVariant)
                        Spacer(Modifier.width(SportKidTheme.spacing.xs))
                        Text(
                            "${race.date.shortLabelWithYear()} · ${race.venue}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                race.photoRes?.let { res ->
                    Spacer(Modifier.width(SportKidTheme.spacing.sm))
                    Box(Modifier.size(52.dp)) {
                        Image(
                            painterResource(res),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.small),
                        )
                        if (race.result in Podium) MedalDot(race.result, size = 20, modifier = Modifier.align(Alignment.BottomEnd))
                    }
                }
            }
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                MetricBox(
                    stringResource(R.string.ath_distance_type),
                    stringResource(R.string.ath_distance_with_type, distanceLabel(race.distance), race.type.label()),
                    Modifier.weight(1f),
                )
                MetricBox(stringResource(R.string.ath_official_time), race.time.short(), Modifier.weight(1f), highlight = race.isRecord)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val medal = race.result.colors()
                Icon(painterResource(UiR.drawable.ic_medal), contentDescription = null, modifier = Modifier.size(16.dp), tint = medal.accent)
                Spacer(Modifier.width(SportKidTheme.spacing.xs))
                Text(
                    placeLabel(race.result),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface,
                )
                val paid = race.payment == PaymentStatus.PAID
                SkPill(
                    stringResource(if (paid) R.string.ath_cost_paid else R.string.ath_cost_pending, amountLabel(race.costPesos)),
                    containerColor = if (paid) SportKidTheme.sportColors.successContainer else SportKidTheme.sportColors.warningContainer,
                    contentColor = colors.onSurface,
                )
            }
        }
    }
}

private val Podium = setOf(MedalResult.GOLD, MedalResult.SILVER, MedalResult.BRONZE)

@Composable
private fun placeLabel(result: MedalResult): String = when (result) {
    MedalResult.GOLD -> stringResource(R.string.ath_place, 1, stringResource(result.labelRes()))
    MedalResult.SILVER -> stringResource(R.string.ath_place, 2, stringResource(result.labelRes()))
    MedalResult.BRONZE -> stringResource(R.string.ath_place, 3, stringResource(result.labelRes()))
    else -> stringResource(result.labelRes())
}

@Composable
private fun MetricBox(label: String, value: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier, shape = MaterialTheme.shapes.medium, color = if (highlight) colors.tertiaryContainer.copy(alpha = 0.5f) else colors.surfaceContainerLow) {
        Column(Modifier.padding(horizontal = SportKidTheme.spacing.sm, vertical = SportKidTheme.spacing.xs)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (highlight) colors.tertiary else colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@SkPreviews
@Composable
private fun AthleticsScreenPreview() = SportKidTheme {
    AthleticsScreen(athlete = AthleticsSamples.sofia, onBack = {}, onNewRace = {}, today = LocalDate.of(2025, 11, 10))
}
