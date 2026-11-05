package com.invex.one.template.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.template.R
import com.invex.one.template.ui.components.MainTab
import com.invex.one.template.ui.components.VuelveMainScaffold
import com.invex.one.template.ui.components.subtleShadow
import com.invex.one.template.ui.theme.StatusDueContainer
import com.invex.one.template.ui.theme.StatusDueDot
import com.invex.one.template.ui.theme.StatusDueText
import com.invex.one.template.ui.theme.StatusFollowUpContainer
import com.invex.one.template.ui.theme.StatusFollowUpDot
import com.invex.one.template.ui.theme.StatusFollowUpText
import com.invex.one.template.ui.theme.StatusOverdueContainer
import com.invex.one.template.ui.theme.StatusOverdueDot
import com.invex.one.template.ui.theme.StatusOverdueText
import com.invex.one.template.ui.theme.TemplateTheme
import com.invex.one.template.ui.theme.VuelveBlue600
import com.invex.one.template.ui.theme.VuelveOnSurface
import com.invex.one.template.ui.theme.VuelveOnSurfaceVariant
import com.invex.one.template.ui.theme.VuelveOutline
import com.invex.one.template.ui.theme.VuelvePrimary
import com.invex.one.template.ui.theme.VuelveSuccess
import com.invex.one.template.ui.theme.VuelveSurfaceContainer
import com.invex.one.template.ui.theme.VuelveSurfaceContainerHigh
import com.invex.one.template.ui.theme.VuelveSurfaceContainerLow
import com.invex.one.template.ui.theme.VuelveVisitStrip
import com.invex.one.template.ui.theme.VuelveWhatsApp

// region Model

/** Retention status shown on priority cards (README §5). Icon is the one Figma pairs with each status. */
enum class RetentionStatus {
    Due,
    Overdue,
    FollowUp,
}

data class PriorityClient(
    val id: String,
    val ownerName: String,
    val petName: String,
    val breed: String,
    @DrawableRes val petPhoto: Int,
    val status: RetentionStatus,
    val lastVisit: String,
    val lastService: String,
    val messagePreview: String,
)

data class MonthResults(
    val monthName: String,
    val contacted: Int,
    val recovered: Int,
    val recoveredTrend: String,
    val conversion: String,
    val conversionGoal: String,
)

data class HomeUiState(
    val userName: String,
    val businessName: String,
    val dateLabel: String,
    val toContactToday: Int,
    val potentialRevenue: String,
    val priorityTotal: Int,
    val urgentCount: Int,
    val priorityClients: List<PriorityClient>,
    val results: MonthResults,
)

/** Sample data from the Figma frame until the screen is backed by a repository. */
val SampleHomeUiState = HomeUiState(
    userName = "Ana",
    businessName = "Patitas Felices",
    dateLabel = "Mar, 24 Oct",
    toContactToday = 8,
    potentialRevenue = "$3,450 MXN",
    priorityTotal = 8,
    urgentCount = 3,
    priorityClients = listOf(
        PriorityClient(
            id = "maria-luna",
            ownerName = "María López",
            petName = "Luna",
            breed = "Cocker Spaniel",
            petPhoto = R.drawable.img_sample_pet_luna,
            status = RetentionStatus.Due,
            lastVisit = "Hace 4 sem (26 Sep)",
            lastService = "Baño y corte completo",
            messagePreview = "“Hola María, ¿agendamos a Luna?...”",
        ),
        PriorityClient(
            id = "jorge-max",
            ownerName = "Jorge Ramírez",
            petName = "Max",
            breed = "Golden Retriever",
            petPhoto = R.drawable.img_sample_pet_max,
            status = RetentionStatus.Overdue,
            lastVisit = "Hace 6 sem (12 Sep)",
            lastService = "Deslanado y baño",
            messagePreview = "“Hola Jorge, extrañamos ver a Max...”",
        ),
        PriorityClient(
            id = "daniela-coco",
            ownerName = "Daniela Torres",
            petName = "Coco",
            breed = "Poodle Toy",
            petPhoto = R.drawable.img_sample_pet_coco,
            status = RetentionStatus.FollowUp,
            lastVisit = "Hace 3 sem (03 Oct)",
            lastService = "Corte higiénico",
            messagePreview = "“Buen día Daniela, ¿cómo sigue Coco?...”",
        ),
    ),
    results = MonthResults(
        monthName = "Octubre",
        contacted = 18,
        recovered = 6,
        recoveredTrend = "+33%",
        conversion = "33%",
        conversionGoal = "25%",
    ),
)

// endregion

/** "Inicio — Oportunidades de hoy" (Figma 90:2326), wrapped in the main app shell. */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onTabSelected: (MainTab) -> Unit,
    onAccountClick: () -> Unit = {},
    onSeeFollowUps: () -> Unit = {},
    onSeeAllPriority: () -> Unit = {},
    onContact: (PriorityClient) -> Unit = {},
    onClientClick: (PriorityClient) -> Unit = {},
    onMetrics: () -> Unit = {},
    onRegisterVisit: () -> Unit = {},
    onClientList: () -> Unit = {},
) {
    VuelveMainScaffold(
        title = stringResource(R.string.home_title),
        selectedTab = MainTab.Home,
        onTabSelected = onTabSelected,
        accountAvatar = painterResource(R.drawable.img_sample_profile),
        onAccountClick = onAccountClick,
    ) { innerPadding ->
        HomeContent(
            state = state,
            contentPadding = innerPadding,
            onSeeFollowUps = onSeeFollowUps,
            onSeeAllPriority = onSeeAllPriority,
            onContact = onContact,
            onClientClick = onClientClick,
            onMetrics = onMetrics,
            onRegisterVisit = onRegisterVisit,
            onClientList = onClientList,
        )
    }
}

@Composable
fun HomeContent(
    state: HomeUiState,
    contentPadding: PaddingValues,
    onSeeFollowUps: () -> Unit,
    onSeeAllPriority: () -> Unit,
    onContact: (PriorityClient) -> Unit,
    onClientClick: (PriorityClient) -> Unit,
    onMetrics: () -> Unit,
    onRegisterVisit: () -> Unit,
    onClientList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
    ) {
        Greeting(
            userName = state.userName,
            dateLabel = state.dateLabel,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
        OpportunityHero(
            count = state.toContactToday,
            potential = state.potentialRevenue,
            onSeeFollowUps = onSeeFollowUps,
            modifier = Modifier.padding(bottom = 24.dp),
        )
        PrioritySection(
            total = state.priorityTotal,
            urgent = state.urgentCount,
            clients = state.priorityClients,
            onSeeAll = onSeeAllPriority,
            onContact = onContact,
            onClientClick = onClientClick,
            modifier = Modifier.padding(bottom = 24.dp),
        )
        ResultsPanel(
            results = state.results,
            businessName = state.businessName,
            onMetrics = onMetrics,
            modifier = Modifier.padding(bottom = 24.dp),
        )
        QuickActions(onRegisterVisit = onRegisterVisit, onClientList = onClientList)
    }
}

// region Greeting

@Composable
private fun Greeting(userName: String, dateLabel: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Row(
                modifier = Modifier.padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_greeting, userName),
                    style = MaterialTheme.typography.headlineSmall.copy(letterSpacing = (-0.33).sp),
                    color = VuelveOnSurface,
                )
                Text(text = stringResource(R.string.home_greeting_emoji), fontSize = 20.sp, lineHeight = 20.sp)
            }
            Text(
                text = stringResource(R.string.home_greeting_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = VuelveOnSurfaceVariant,
            )
        }
        val pillShape = CircleShape
        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .subtleShadow(pillShape)
                .background(VuelveSurfaceContainer, pillShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(painter = painterResource(R.drawable.ic_home_calendar), contentDescription = null)
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = VuelveOnSurfaceVariant,
            )
        }
    }
}

// endregion

// region Hero (O5)

@Composable
private fun OpportunityHero(
    count: Int,
    potential: String,
    onSeeFollowUps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f),
            )
            .background(
                Brush.linearGradient(
                    0f to Color.White,
                    0.5f to Color.White,
                    1f to VuelveSurfaceContainerLow,
                ),
                shape,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(VuelveBlue600, CircleShape),
                )
                Text(
                    text = stringResource(R.string.home_hero_overline),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                    ),
                    color = VuelvePrimary,
                )
            }
            Image(painter = painterResource(R.drawable.ic_home_paw_hero), contentDescription = null)
        }
        Row(modifier = Modifier.height(47.dp)) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displaySmall.copy(lineHeight = 30.sp),
                color = VuelveOnSurface,
            )
            Column(modifier = Modifier.padding(start = 12.dp, top = 11.dp)) {
                Text(
                    text = stringResource(R.string.home_hero_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = VuelveOnSurface,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Image(painter = painterResource(R.drawable.ic_home_trend_up), contentDescription = null)
                    Text(
                        text = stringResource(R.string.home_hero_potential, potential),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = VuelveSuccess,
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.home_hero_description),
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.5.sp),
            color = VuelveOnSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Button(
            onClick = onSeeFollowUps,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .subtleShadow(RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VuelveBlue600, contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_home_followups_btn), contentDescription = null)
                Text(
                    text = stringResource(R.string.home_hero_cta, count),
                    style = MaterialTheme.typography.labelLarge,
                )
                Image(painter = painterResource(R.drawable.ic_vuelve_arrow_forward), contentDescription = null)
            }
        }
    }
}

// endregion

// region Priority clients (O6)

@Composable
private fun PrioritySection(
    total: Int,
    urgent: Int,
    clients: List<PriorityClient>,
    onSeeAll: () -> Unit,
    onContact: (PriorityClient) -> Unit,
    onClientClick: (PriorityClient) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_priority_title),
                    style = MaterialTheme.typography.titleLarge.copy(letterSpacing = (-0.18).sp),
                    color = VuelveOnSurface,
                )
                Text(
                    text = pluralStringResource(R.plurals.home_priority_urgent, urgent, urgent),
                    style = MaterialTheme.typography.labelSmall,
                    color = VuelveOnSurfaceVariant,
                    modifier = Modifier
                        .background(VuelveSurfaceContainerHigh, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
            Row(
                modifier = Modifier.clickable(onClick = onSeeAll, role = Role.Button),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_priority_see_all, total),
                    style = MaterialTheme.typography.labelMedium,
                    color = VuelvePrimary,
                )
                Image(painter = painterResource(R.drawable.ic_home_chevron_right), contentDescription = null)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            clients.forEach { client ->
                PriorityClientCard(
                    client = client,
                    onContact = { onContact(client) },
                    onClick = { onClientClick(client) },
                )
            }
        }
    }
}

@Composable
private fun PriorityClientCard(client: PriorityClient, onContact: () -> Unit, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow(shape)
            .clip(shape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(client.petPhoto),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(VuelveSurfaceContainer),
                )
                Column {
                    Text(
                        text = client.ownerName,
                        style = MaterialTheme.typography.titleMedium,
                        color = VuelveOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = client.petName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = VuelveOnSurface,
                        )
                        Text(
                            text = stringResource(R.string.vuelve_pet_breed, client.breed),
                            style = MaterialTheme.typography.bodySmall,
                            color = VuelveOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            StatusBadge(status = client.status, modifier = Modifier.padding(start = 8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VuelveVisitStrip, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(painter = painterResource(client.status.visitIcon), contentDescription = null)
                Text(
                    text = client.lastVisit,
                    style = MaterialTheme.typography.bodySmall,
                    color = VuelveOnSurfaceVariant,
                )
            }
            Text(
                text = client.lastService,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = VuelveOnSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = client.messagePreview,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = VuelveOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = 170.dp),
            )
            val contactDescription = stringResource(R.string.home_contact_description, client.ownerName)
            Button(
                onClick = onContact,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(36.dp)
                    .subtleShadow(RoundedCornerShape(8.dp))
                    .semantics { contentDescription = contactDescription },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VuelveWhatsApp, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Image(painter = painterResource(R.drawable.ic_home_chat), contentDescription = null)
                    Text(text = stringResource(R.string.home_contact), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private val RetentionStatus.visitIcon: Int
    @DrawableRes get() = when (this) {
        RetentionStatus.Due -> R.drawable.ic_home_history
        RetentionStatus.Overdue -> R.drawable.ic_home_warning
        RetentionStatus.FollowUp -> R.drawable.ic_home_event
    }

@Composable
private fun StatusBadge(status: RetentionStatus, modifier: Modifier = Modifier) {
    val (container, dot, text, label) = when (status) {
        RetentionStatus.Due -> StatusStyle(StatusDueContainer, StatusDueDot, StatusDueText, R.string.home_status_due)
        RetentionStatus.Overdue ->
            StatusStyle(StatusOverdueContainer, StatusOverdueDot, StatusOverdueText, R.string.home_status_overdue)
        RetentionStatus.FollowUp ->
            StatusStyle(StatusFollowUpContainer, StatusFollowUpDot, StatusFollowUpText, R.string.home_status_follow_up)
    }
    Row(
        modifier = modifier
            .background(container, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dot, CircleShape),
        )
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 16.5.sp, letterSpacing = 0.sp),
            color = text,
        )
    }
}

private data class StatusStyle(val container: Color, val dot: Color, val text: Color, val label: Int)

// endregion

// region Results (O7)

@Composable
private fun ResultsPanel(
    results: MonthResults,
    businessName: String,
    onMetrics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .subtleShadow(shape)
            .background(Color.White, shape)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_home_insights), contentDescription = null)
                Text(
                    text = stringResource(R.string.home_results_title, results.monthName),
                    style = MaterialTheme.typography.titleMedium,
                    color = VuelveOnSurface,
                )
            }
            Text(
                text = stringResource(R.string.home_results_metrics),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = VuelvePrimary,
                modifier = Modifier.clickable(onClick = onMetrics, role = Role.Button),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatTile(
                label = stringResource(R.string.home_results_contacted),
                value = results.contacted.toString(),
            ) {
                Text(
                    text = stringResource(R.string.home_results_contacted_unit),
                    style = TileFootnoteStyle,
                    color = VuelveOnSurfaceVariant,
                )
            }
            StatTile(
                label = stringResource(R.string.home_results_recovered),
                value = results.recovered.toString(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(R.drawable.ic_home_arrow_up_small), contentDescription = null)
                    Text(
                        text = results.recoveredTrend,
                        style = TileFootnoteStyle.copy(fontWeight = FontWeight.Bold),
                        color = VuelveSuccess,
                    )
                }
            }
            StatTile(
                label = stringResource(R.string.home_results_conversion),
                value = results.conversion,
                valueColor = VuelvePrimary,
            ) {
                Text(
                    text = stringResource(R.string.home_results_goal, results.conversionGoal),
                    style = TileFootnoteStyle.copy(fontWeight = FontWeight.Medium),
                    color = VuelveSuccess,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            Image(painter = painterResource(R.drawable.ic_home_info_small), contentDescription = null)
            Text(
                text = stringResource(R.string.home_results_footnote, businessName),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.5.sp,
                    letterSpacing = 0.sp,
                ),
                color = VuelveOutline,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val TileFootnoteStyle = TextStyle(fontSize = 10.sp, lineHeight = 15.sp)

@Composable
private fun RowScope.StatTile(
    label: String,
    value: String,
    valueColor: Color = VuelveOnSurface,
    footnote: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .background(VuelveSurfaceContainerLow, RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = VuelveOnSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.18).sp),
            color = valueColor,
            modifier = Modifier.padding(vertical = 2.dp),
        )
        footnote()
    }
}

// endregion

// region Quick actions (O8)

@Composable
private fun QuickActions(onRegisterVisit: () -> Unit, onClientList: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.home_quick_actions),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
            color = VuelveOnSurfaceVariant,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionButton(
                icon = R.drawable.ic_home_person_add,
                label = stringResource(R.string.home_quick_register_visit),
                onClick = onRegisterVisit,
            )
            QuickActionButton(
                icon = R.drawable.ic_home_contacts,
                label = stringResource(R.string.home_quick_client_list),
                onClick = onClientList,
            )
        }
    }
}

@Composable
private fun RowScope.QuickActionButton(@DrawableRes icon: Int, label: String, onClick: () -> Unit) {
    val shape: Shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .subtleShadow(shape)
            .clip(shape)
            .background(VuelveSurfaceContainerHigh)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Image(painter = painterResource(icon), contentDescription = null)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = VuelveOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// endregion

@Preview(showBackground = true, widthDp = 390, heightDp = 1589)
@Composable
private fun HomeScreenPreview() {
    TemplateTheme {
        HomeScreen(state = SampleHomeUiState, onTabSelected = {})
    }
}
