package com.invex.one.template.ui.followups

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.template.R
import com.invex.one.template.ui.components.MainTab
import com.invex.one.template.ui.components.VuelveMainScaffold
import com.invex.one.template.ui.components.subtleShadow
import com.invex.one.template.ui.theme.FollowUpCardShadow
import com.invex.one.template.ui.theme.StatusContactContainer
import com.invex.one.template.ui.theme.StatusDueText
import com.invex.one.template.ui.theme.StatusOverdueContainer
import com.invex.one.template.ui.theme.StatusOverdueDot
import com.invex.one.template.ui.theme.StatusOverdueText
import com.invex.one.template.ui.theme.StatusUpToDateDot
import com.invex.one.template.ui.theme.TemplateTheme
import com.invex.one.template.ui.theme.VuelveBlue600
import com.invex.one.template.ui.theme.VuelveMint
import com.invex.one.template.ui.theme.VuelveOnBlue
import com.invex.one.template.ui.theme.VuelveOnMint
import com.invex.one.template.ui.theme.VuelveOnPrimaryContainer
import com.invex.one.template.ui.theme.VuelveOnSurface
import com.invex.one.template.ui.theme.VuelveOnSurfaceVariant
import com.invex.one.template.ui.theme.VuelveOutlineSoft
import com.invex.one.template.ui.theme.VuelvePrimary
import com.invex.one.template.ui.theme.VuelvePrimaryContainer
import com.invex.one.template.ui.theme.VuelveSecondaryContainer
import com.invex.one.template.ui.theme.VuelveSurfaceContainer
import com.invex.one.template.ui.theme.VuelveSurfaceContainerHigh
import com.invex.one.template.ui.theme.VuelveSurfaceContainerLow
import com.invex.one.template.ui.theme.VuelveWhatsAppDeep

// region Model

enum class FollowUpUrgency { Overdue, Today }

/** Icon Figma pairs with each "Motivo" line. */
enum class FollowUpReasonIcon(@DrawableRes val res: Int) {
    Warning(R.drawable.ic_followups_warning),
    PriorityHigh(R.drawable.ic_followups_priority_high),
    Schedule(R.drawable.ic_followups_schedule),
    Grooming(R.drawable.ic_followups_scissors),
}

data class FollowUp(
    val id: String,
    val ownerName: String,
    val petName: String,
    val breed: String,
    @DrawableRes val petPhoto: Int,
    val urgency: FollowUpUrgency,
    val daysLate: Int = 0,
    val lastVisit: String,
    val suggestedDate: String,
    val reason: String,
    val reasonIcon: FollowUpReasonIcon,
    /** Shows the paw mark next to the owner name (frequent client). */
    val isFrequent: Boolean = false,
)

data class UpcomingFollowUp(
    val id: String,
    val ownerName: String,
    val petName: String,
    @DrawableRes val petPhoto: Int,
    val suggestedService: String,
    val suggestedDate: String,
)

enum class FollowUpFilter { All, Today, Overdue, Upcoming, ContactedThisMonth }

data class FollowUpsUiState(
    val readyCount: Int,
    val filterCounts: Map<FollowUpFilter, Int>,
    val shiftContacted: Int,
    val shiftGoal: Int,
    val estimatedResponseRate: Int,
    val overdue: List<FollowUp>,
    val today: List<FollowUp>,
    val upcoming: List<UpcomingFollowUp>,
)

/** Sample data from the Figma frame until the screen is backed by a repository. */
val SampleFollowUpsUiState = FollowUpsUiState(
    readyCount = 8,
    filterCounts = mapOf(
        FollowUpFilter.All to 8,
        FollowUpFilter.Today to 4,
        FollowUpFilter.Overdue to 3,
        FollowUpFilter.Upcoming to 1,
        FollowUpFilter.ContactedThisMonth to 18,
    ),
    shiftContacted = 5,
    shiftGoal = 12,
    estimatedResponseRate = 78,
    overdue = listOf(
        FollowUp(
            id = "jorge-max", ownerName = "Jorge Ramírez", petName = "Max", breed = "Golden Retriever",
            petPhoto = R.drawable.img_sample_followup_max, urgency = FollowUpUrgency.Overdue, daysLate = 14,
            lastVisit = "12 Sep (Hace 6 sem)", suggestedDate = "10 Oct",
            reason = "Hace tiempo que no regresa", reasonIcon = FollowUpReasonIcon.Warning, isFrequent = true,
        ),
        FollowUp(
            id = "rodrigo-toby", ownerName = "Rodrigo Cruz", petName = "Toby", breed = "Pug",
            petPhoto = R.drawable.img_sample_followup_toby, urgency = FollowUpUrgency.Overdue, daysLate = 21,
            lastVisit = "05 Sep (Hace 7 sem)", suggestedDate = "03 Oct",
            reason = "Cliente frecuente inactivo", reasonIcon = FollowUpReasonIcon.PriorityHigh,
        ),
    ),
    today = listOf(
        FollowUp(
            id = "maria-luna", ownerName = "María López", petName = "Luna", breed = "Cocker Spaniel",
            petPhoto = R.drawable.img_sample_followup_luna, urgency = FollowUpUrgency.Today,
            lastVisit = "26 Sep (4 sem)", suggestedDate = "24 Oct (Hoy)",
            reason = "Ya le toca su siguiente servicio (Baño y corte)", reasonIcon = FollowUpReasonIcon.Schedule,
        ),
        FollowUp(
            id = "daniela-coco", ownerName = "Daniela Torres", petName = "Coco", breed = "Poodle Toy",
            petPhoto = R.drawable.img_sample_followup_coco, urgency = FollowUpUrgency.Today,
            lastVisit = "03 Oct (3 sem)", suggestedDate = "24 Oct (Hoy)",
            reason = "Mantenimiento y corte higiénico", reasonIcon = FollowUpReasonIcon.Grooming,
        ),
    ),
    upcoming = listOf(
        UpcomingFollowUp(
            id = "carmen-milo", ownerName = "Carmen Silva", petName = "Milo",
            petPhoto = R.drawable.img_sample_followup_milo,
            suggestedService = "Baño mensual sugerido", suggestedDate = "Sugerido: 28 Oct (en 4 días)",
        ),
    ),
)

// endregion

/** "Seguimientos — Prioridades diarias" (Figma 92:2885), wrapped in the main app shell. */
@Composable
fun FollowUpsScreen(
    state: FollowUpsUiState,
    onTabSelected: (MainTab) -> Unit,
    onAccountClick: () -> Unit = {},
    onWhatsApp: (FollowUp) -> Unit = {},
    onViewProfile: (FollowUp) -> Unit = {},
    onViewUpcoming: (UpcomingFollowUp) -> Unit = {},
) {
    var filter by rememberSaveable { mutableStateOf(FollowUpFilter.All) }
    // Postponing hides the card locally until there is a backend to persist it.
    var postponed by rememberSaveable { mutableStateOf(setOf<String>()) }

    VuelveMainScaffold(
        title = stringResource(R.string.followups_title),
        selectedTab = MainTab.FollowUps,
        onTabSelected = onTabSelected,
        accountAvatar = painterResource(R.drawable.img_sample_profile),
        onAccountClick = onAccountClick,
    ) { innerPadding ->
        FollowUpsContent(
            state = state.copy(overdue = state.overdue.filterNot { it.id in postponed }),
            filter = filter,
            onFilterChange = { filter = it },
            contentPadding = innerPadding,
            onWhatsApp = onWhatsApp,
            onPostpone = { postponed = postponed + it.id },
            onViewProfile = onViewProfile,
            onViewUpcoming = onViewUpcoming,
        )
    }
}

@Composable
fun FollowUpsContent(
    state: FollowUpsUiState,
    filter: FollowUpFilter,
    onFilterChange: (FollowUpFilter) -> Unit,
    contentPadding: PaddingValues,
    onWhatsApp: (FollowUp) -> Unit,
    onPostpone: (FollowUp) -> Unit,
    onViewProfile: (FollowUp) -> Unit,
    onViewUpcoming: (UpcomingFollowUp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showOverdue = filter == FollowUpFilter.All || filter == FollowUpFilter.Overdue
    val showToday = filter == FollowUpFilter.All || filter == FollowUpFilter.Today
    val showUpcoming = filter == FollowUpFilter.All || filter == FollowUpFilter.Upcoming

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(bottom = 32.dp),
    ) {
        ScreenHeader(
            readyCount = state.readyCount,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        )
        FilterChips(selected = filter, counts = state.filterCounts, onSelect = onFilterChange)
        ShiftGoal(
            contacted = state.shiftContacted,
            goal = state.shiftGoal,
            responseRate = state.estimatedResponseRate,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (showOverdue && state.overdue.isNotEmpty()) {
                Section(
                    title = stringResource(R.string.followups_section_overdue),
                    hint = stringResource(R.string.followups_section_overdue_hint),
                    accent = StatusOverdueDot,
                    trailing = {
                        CountPill(
                            text = pluralStringResource(
                                R.plurals.followups_section_overdue_count,
                                state.overdue.size,
                                state.overdue.size,
                            ),
                            container = StatusOverdueContainer,
                            content = StatusOverdueText,
                        )
                    },
                ) {
                    state.overdue.forEach { item ->
                        FollowUpCard(
                            item = item,
                            onWhatsApp = { onWhatsApp(item) },
                            onSecondary = { onPostpone(item) },
                        )
                    }
                }
            }
            if (showToday && state.today.isNotEmpty()) {
                Section(
                    title = stringResource(R.string.followups_section_today),
                    hint = stringResource(R.string.followups_section_today_hint),
                    accent = VuelvePrimary,
                    trailing = {
                        CountPill(
                            text = pluralStringResource(
                                R.plurals.followups_section_today_count,
                                state.today.size,
                                state.today.size,
                            ),
                            container = VuelvePrimaryContainer,
                            content = VuelveOnPrimaryContainer,
                        )
                    },
                ) {
                    state.today.forEach { item ->
                        FollowUpCard(
                            item = item,
                            onWhatsApp = { onWhatsApp(item) },
                            onSecondary = { onViewProfile(item) },
                        )
                    }
                }
            }
            if (showUpcoming && state.upcoming.isNotEmpty()) {
                Section(
                    title = stringResource(R.string.followups_section_upcoming),
                    hint = null,
                    accent = VuelveOutlineSoft,
                    trailing = {
                        Text(
                            text = pluralStringResource(
                                R.plurals.followups_section_upcoming_count,
                                state.upcoming.size,
                                state.upcoming.size,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = VuelveOnSurfaceVariant,
                        )
                    },
                ) {
                    state.upcoming.forEach { item ->
                        UpcomingCard(item = item, onView = { onViewUpcoming(item) })
                    }
                }
            }
            val nothingShown = !(showOverdue && state.overdue.isNotEmpty()) &&
                !(showToday && state.today.isNotEmpty()) &&
                !(showUpcoming && state.upcoming.isNotEmpty())
            if (nothingShown) {
                Text(
                    text = stringResource(R.string.followups_empty_filter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VuelveOnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                )
            }
            GoalBanner()
        }
    }
}

// region Header, filters, goal (O12)

@Composable
private fun ScreenHeader(readyCount: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.followups_title),
                style = MaterialTheme.typography.headlineSmall.copy(letterSpacing = (-0.33).sp),
                color = VuelveOnSurface,
            )
            Row(
                modifier = Modifier
                    .subtleShadow(CircleShape)
                    .background(VuelveMint, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(StatusUpToDateDot, CircleShape),
                )
                Text(
                    text = stringResource(R.string.followups_live),
                    style = MaterialTheme.typography.labelSmall,
                    color = VuelveOnMint,
                )
            }
        }
        Text(
            text = stringResource(R.string.followups_subtitle, readyCount),
            style = MaterialTheme.typography.bodyMedium,
            color = VuelveOnSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun FilterChips(
    selected: FollowUpFilter,
    counts: Map<FollowUpFilter, Int>,
    onSelect: (FollowUpFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FollowUpFilter.entries.forEach { option ->
            val count = counts[option] ?: 0
            val isSelected = option == selected
            val (label, pill) = when (option) {
                FollowUpFilter.All ->
                    stringResource(R.string.followups_filter_all) to (VuelvePrimaryContainer to VuelveOnPrimaryContainer)
                FollowUpFilter.Today ->
                    stringResource(R.string.followups_filter_today) to (StatusContactContainer to StatusDueText)
                FollowUpFilter.Overdue ->
                    stringResource(R.string.followups_filter_overdue) to (StatusOverdueContainer to StatusOverdueText)
                FollowUpFilter.Upcoming ->
                    stringResource(R.string.followups_filter_upcoming) to (VuelveSecondaryContainer to VuelveOnSurfaceVariant)
                FollowUpFilter.ContactedThisMonth ->
                    stringResource(R.string.followups_filter_contacted, count) to null
            }
            OperationalChip(
                label = label,
                count = if (pill != null) count else null,
                pillColors = if (isSelected) VuelveBlue600 to VuelveOnBlue else pill,
                selected = isSelected,
                onClick = { onSelect(option) },
            )
        }
    }
}

@Composable
private fun OperationalChip(
    label: String,
    count: Int?,
    pillColors: Pair<Color, Color>?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .then(if (selected) Modifier.subtleShadow(CircleShape) else Modifier)
            .clip(CircleShape)
            .background(if (selected) VuelvePrimary else VuelveSurfaceContainer)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else VuelveOnSurfaceVariant,
            maxLines = 1,
        )
        if (count != null && pillColors != null) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp,
                    letterSpacing = 0.24.sp,
                ),
                color = pillColors.second,
                modifier = Modifier
                    .background(pillColors.first, CircleShape)
                    .padding(horizontal = 6.dp),
            )
        }
    }
}

@Composable
private fun ShiftGoal(contacted: Int, goal: Int, responseRate: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .subtleShadow(shape)
            .background(VuelveSurfaceContainerLow, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(VuelvePrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(painter = painterResource(R.drawable.ic_followups_bolt), contentDescription = null)
            }
            Column {
                Text(
                    text = stringResource(R.string.followups_goal_title, contacted, goal),
                    style = MaterialTheme.typography.labelMedium,
                    color = VuelveOnSurface,
                )
                Text(
                    text = stringResource(R.string.followups_goal_rate, responseRate),
                    style = MaterialTheme.typography.bodySmall,
                    color = VuelveOnSurfaceVariant,
                )
            }
        }
        val progress = if (goal > 0) (contacted.toFloat() / goal).coerceIn(0f, 1f) else 0f
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .width(64.dp)
                .height(8.dp)
                .clip(CircleShape)
                .background(VuelveSecondaryContainer),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(CircleShape)
                    .background(VuelvePrimary),
            )
        }
    }
}

// endregion

// region Sections + cards (O11, M10, M11, M21, M22)

@Composable
private fun Section(
    title: String,
    hint: String?,
    accent: Color,
    trailing: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accent, CircleShape),
                )
                Text(
                    text = buildAnnotatedString {
                        append(title)
                        if (hint != null) {
                            withStyle(SpanStyle(fontSize = 12.sp, letterSpacing = 0.12.sp, color = accent)) {
                                append(hint)
                            }
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = VuelveOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(modifier = Modifier.padding(start = 8.dp)) { trailing() }
        }
        content()
    }
}

@Composable
private fun CountPill(text: String, container: Color, content: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = content,
        modifier = Modifier
            .background(container, CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

private fun Modifier.cardShadow(shape: Shape, elevation: Int) =
    shadow(elevation = elevation.dp, shape = shape, ambientColor = FollowUpCardShadow, spotColor = FollowUpCardShadow)

@Composable
private fun FollowUpCard(item: FollowUp, onWhatsApp: () -> Unit, onSecondary: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val isOverdue = item.urgency == FollowUpUrgency.Overdue
    val accent = if (isOverdue) StatusOverdueDot else VuelvePrimary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .cardShadow(shape, elevation = 4)
            .clip(shape)
            .background(Color.White)
            .drawBehind { drawRect(accent, size = Size(6.dp.toPx(), size.height)) }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PetPhoto(photo = item.petPhoto, size = 48)
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = item.ownerName,
                            style = MaterialTheme.typography.titleMedium,
                            color = VuelveOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (item.isFrequent) {
                            Icon(
                                painter = painterResource(R.drawable.ic_vuelve_paw_small),
                                contentDescription = stringResource(R.string.followups_loyal_client),
                                tint = VuelveWhatsAppDeep,
                            )
                        }
                    }
                    Text(
                        text = "${item.petName} • ${item.breed}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = VuelveOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            CountPill(
                text = if (isOverdue) {
                    stringResource(R.string.followups_days_late, item.daysLate)
                } else {
                    stringResource(R.string.followups_today_badge)
                },
                container = if (isOverdue) StatusOverdueContainer else VuelveBlue600,
                content = if (isOverdue) StatusOverdueText else VuelveOnBlue,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VuelveSurfaceContainerLow, RoundedCornerShape(8.dp))
                .padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledValue(
                    label = stringResource(R.string.followups_last_visit),
                    value = item.lastVisit,
                    valueColor = VuelveOnSurface,
                    modifier = Modifier.weight(1f),
                )
                LabeledValue(
                    label = stringResource(
                        if (isOverdue) R.string.followups_suggested_return else R.string.followups_suggested_date,
                    ),
                    value = item.suggestedDate,
                    valueColor = accent,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(painter = painterResource(item.reasonIcon.res), contentDescription = null)
                Text(
                    text = stringResource(R.string.followups_reason),
                    style = MaterialTheme.typography.labelSmall,
                    color = VuelveOnSurfaceVariant,
                )
                Text(
                    text = item.reason,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = VuelveOnSurface,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onWhatsApp,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .subtleShadow(RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VuelveWhatsAppDeep, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_followups_chat), contentDescription = null)
                Text(
                    text = stringResource(R.string.followups_whatsapp),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            TonalAction(
                icon = if (isOverdue) R.drawable.ic_followups_snooze else R.drawable.ic_vuelve_person,
                iconTint = VuelveOnSurfaceVariant,
                label = stringResource(if (isOverdue) R.string.followups_postpone else R.string.followups_view_profile),
                onClick = onSecondary,
            )
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Text(
        text = buildAnnotatedString {
            append(label)
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = valueColor)) { append(value) }
        },
        style = MaterialTheme.typography.bodySmall,
        color = VuelveOnSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun TonalAction(@DrawableRes icon: Int, iconTint: Color, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(VuelveSurfaceContainer)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = iconTint)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = VuelveOnSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun PetPhoto(@DrawableRes photo: Int, size: Int) {
    Image(
        painter = painterResource(photo),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size.dp)
            .shadow(1.dp, CircleShape)
            .clip(CircleShape)
            .background(VuelveSurfaceContainerHigh),
    )
}

@Composable
private fun UpcomingCard(item: UpcomingFollowUp, onView: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cardShadow(shape, elevation = 2)
            .background(Color.White, shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PetPhoto(photo = item.petPhoto, size = 44)
            Column {
                Text(
                    text = "${item.ownerName} • ${item.petName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = VuelveOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.suggestedService,
                    style = MaterialTheme.typography.bodySmall,
                    color = VuelveOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.suggestedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = VuelvePrimary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(VuelveSurfaceContainer)
                .clickable(onClick = onView, role = Role.Button)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.followups_view),
                style = MaterialTheme.typography.labelMedium,
                color = VuelveOnSurface,
            )
            Icon(
                painter = painterResource(R.drawable.ic_vuelve_chevron_right_small),
                contentDescription = null,
                tint = VuelveOnSurface,
            )
        }
    }
}

// endregion

// region Goal banner (O20)

@Composable
private fun GoalBanner() {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow(shape)
            .background(VuelveSurfaceContainerLow, shape)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .subtleShadow(CircleShape)
                .background(VuelveMint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(painter = painterResource(R.drawable.ic_followups_check_circle), contentDescription = null)
        }
        Text(
            text = stringResource(R.string.followups_goal_banner_title),
            style = MaterialTheme.typography.titleMedium,
            color = VuelveOnSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.followups_goal_banner_body),
            style = MaterialTheme.typography.bodySmall,
            color = VuelveOnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(painter = painterResource(R.drawable.ic_followups_trend_check), contentDescription = null)
            Text(
                text = stringResource(R.string.followups_goal_banner_benefit),
                style = MaterialTheme.typography.labelSmall,
                color = VuelveWhatsAppDeep,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// endregion

@Preview(showBackground = true, widthDp = 390, heightDp = 2000)
@Composable
private fun FollowUpsScreenPreview() {
    TemplateTheme {
        FollowUpsScreen(state = SampleFollowUpsUiState, onTabSelected = {})
    }
}
