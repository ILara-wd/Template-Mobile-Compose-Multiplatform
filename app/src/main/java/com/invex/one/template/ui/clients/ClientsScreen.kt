package com.invex.one.template.ui.clients

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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.invex.one.template.ui.theme.StatusContactContainer
import com.invex.one.template.ui.theme.StatusDueText
import com.invex.one.template.ui.theme.StatusOverdueContainer
import com.invex.one.template.ui.theme.StatusOverdueDot
import com.invex.one.template.ui.theme.StatusOverdueText
import com.invex.one.template.ui.theme.StatusUpToDateContainer
import com.invex.one.template.ui.theme.StatusUpToDateDot
import com.invex.one.template.ui.theme.StatusUpToDateText
import com.invex.one.template.ui.theme.TemplateTheme
import com.invex.one.template.ui.theme.VuelveBlue600
import com.invex.one.template.ui.theme.VuelveMint
import com.invex.one.template.ui.theme.VuelveMintStrong
import com.invex.one.template.ui.theme.VuelveOnMintStrong
import com.invex.one.template.ui.theme.VuelveOnPrimaryContainer
import com.invex.one.template.ui.theme.VuelveOnSurface
import com.invex.one.template.ui.theme.VuelveOnSurfaceVariant
import com.invex.one.template.ui.theme.VuelveOutline
import com.invex.one.template.ui.theme.VuelvePrimary
import com.invex.one.template.ui.theme.VuelvePrimaryContainer
import com.invex.one.template.ui.theme.VuelvePrimaryFixedDim
import com.invex.one.template.ui.theme.VuelveSecondaryContainer
import com.invex.one.template.ui.theme.VuelveSurfaceContainer
import com.invex.one.template.ui.theme.VuelveSurfaceContainerHigh
import com.invex.one.template.ui.theme.VuelveSurfaceContainerLow
import java.text.Normalizer

// region Model

/** Retention status as shown in the directory (README §5). */
enum class ClientStatus { ToContact, Overdue, FollowUpToday, UpToDate }

/** Initials avatar palettes used by the Figma rows. */
enum class AvatarTone(val container: Color, val content: Color) {
    Primary(VuelvePrimaryContainer, VuelveOnPrimaryContainer),
    Neutral(VuelveSurfaceContainerHigh, VuelveOnSurfaceVariant),
    Strong(VuelvePrimaryFixedDim, VuelveOnPrimaryContainer),
    Soft(VuelveSecondaryContainer, VuelveOnSurfaceVariant),
    Success(VuelveMintStrong, VuelveOnMintStrong),
}

enum class ClientFilter { All, ToContact, Inactive, UpToDate }

data class ClientRow(
    val id: String,
    val ownerName: String,
    val petName: String,
    val breed: String,
    val status: ClientStatus,
    val overdueDays: Int = 0,
    /** Display date of the suggested return, or null when it is today. */
    val suggestedDate: String?,
    val lastVisitSummary: String,
    val avatarTone: AvatarTone,
) {
    val initials: String
        get() = ownerName.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
}

data class ClientsUiState(
    val totalRegistered: Int,
    val toContactToday: Int,
    val upToDate: Int,
    val filterCounts: Map<ClientFilter, Int>,
    val clients: List<ClientRow>,
)

/** Which rows each filter chip shows. "Sin actividad" groups clients that are overdue. */
private fun ClientFilter.matches(status: ClientStatus): Boolean = when (this) {
    ClientFilter.All -> true
    ClientFilter.ToContact -> status == ClientStatus.ToContact || status == ClientStatus.FollowUpToday
    ClientFilter.Inactive -> status == ClientStatus.Overdue
    ClientFilter.UpToDate -> status == ClientStatus.UpToDate
}

private fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase()

fun ClientsUiState.visibleClients(filter: ClientFilter, query: String): List<ClientRow> {
    val needle = query.trim().normalizedForSearch()
    return clients.filter { client ->
        filter.matches(client.status) &&
            (
                needle.isEmpty() ||
                    client.ownerName.normalizedForSearch().contains(needle) ||
                    client.petName.normalizedForSearch().contains(needle)
                )
    }
}

/** Sample data from the Figma frame until the screen is backed by a repository. */
val SampleClientsUiState = ClientsUiState(
    totalRegistered = 48,
    toContactToday = 8,
    upToDate = 28,
    filterCounts = mapOf(
        ClientFilter.All to 48,
        ClientFilter.ToContact to 8,
        ClientFilter.Inactive to 12,
        ClientFilter.UpToDate to 28,
    ),
    clients = listOf(
        ClientRow(
            id = "maria-luna", ownerName = "María López", petName = "Luna", breed = "Cocker Spaniel",
            status = ClientStatus.ToContact, suggestedDate = null,
            lastVisitSummary = "26 Sep (hace 4 sem) • Baño y corte", avatarTone = AvatarTone.Primary,
        ),
        ClientRow(
            id = "jorge-max", ownerName = "Jorge Ramírez", petName = "Max", breed = "Golden Retriever",
            status = ClientStatus.Overdue, overdueDays = 14, suggestedDate = "10 Oct",
            lastVisitSummary = "12 Sep (hace 6 sem) • Deslanado", avatarTone = AvatarTone.Neutral,
        ),
        ClientRow(
            id = "daniela-coco", ownerName = "Daniela Torres", petName = "Coco", breed = "Poodle Toy",
            status = ClientStatus.FollowUpToday, suggestedDate = "24 Oct",
            lastVisitSummary = "03 Oct (hace 3 sem) • Corte higiénico", avatarTone = AvatarTone.Strong,
        ),
        ClientRow(
            id = "luis-rocky", ownerName = "Luis Mendoza", petName = "Rocky", breed = "Schnauzer",
            status = ClientStatus.UpToDate, suggestedDate = "15 Nov",
            lastVisitSummary = "18 Oct (hace 6 días) • Baño completo", avatarTone = AvatarTone.Success,
        ),
        ClientRow(
            id = "sofia-bella", ownerName = "Sofía Morales", petName = "Bella", breed = "Shih Tzu",
            status = ClientStatus.ToContact, suggestedDate = "22 Oct",
            lastVisitSummary = "20 Sep (hace 5 sem) • Baño y corte", avatarTone = AvatarTone.Soft,
        ),
    ),
)

// endregion

/** "Clientes — Directorio y Estados" (Figma 91:2586), wrapped in the main app shell. */
@Composable
fun ClientsScreen(
    state: ClientsUiState,
    onTabSelected: (MainTab) -> Unit,
    onAccountClick: () -> Unit = {},
    onNewClient: () -> Unit = {},
    onClientClick: (ClientRow) -> Unit = {},
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(ClientFilter.All) }
    val visible = remember(state, filter, query) { state.visibleClients(filter, query) }

    VuelveMainScaffold(
        title = stringResource(R.string.clients_title),
        selectedTab = MainTab.Clients,
        onTabSelected = onTabSelected,
        accountAvatar = painterResource(R.drawable.img_sample_profile),
        onAccountClick = onAccountClick,
    ) { innerPadding ->
        ClientsContent(
            state = state,
            visibleClients = visible,
            query = query,
            onQueryChange = { query = it },
            filter = filter,
            onFilterChange = { filter = it },
            contentPadding = innerPadding,
            onNewClient = onNewClient,
            onClientClick = onClientClick,
        )
    }
}

@Composable
fun ClientsContent(
    state: ClientsUiState,
    visibleClients: List<ClientRow>,
    query: String,
    onQueryChange: (String) -> Unit,
    filter: ClientFilter,
    onFilterChange: (ClientFilter) -> Unit,
    contentPadding: PaddingValues,
    onNewClient: () -> Unit,
    onClientClick: (ClientRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(bottom = 32.dp),
    ) {
        val sectionPadding = Modifier.padding(horizontal = 16.dp)
        DirectoryHeader(
            total = state.totalRegistered,
            onNewClient = onNewClient,
            modifier = sectionPadding.padding(top = 4.dp),
        )
        Row(
            modifier = sectionPadding.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KpiCard(
                label = stringResource(R.string.clients_kpi_to_contact),
                value = stringResource(R.string.clients_kpi_to_contact_value, state.toContactToday),
                icon = R.drawable.ic_clients_bell,
                iconTint = Color.Unspecified,
                circleColor = VuelveMint.copy(alpha = 0.5f),
            )
            KpiCard(
                label = stringResource(R.string.clients_kpi_up_to_date),
                value = stringResource(R.string.clients_kpi_up_to_date_value, state.upToDate),
                icon = R.drawable.ic_vuelve_verified,
                iconTint = VuelveOnPrimaryContainer,
                circleColor = VuelvePrimaryContainer,
            )
        }
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            modifier = sectionPadding.padding(top = 16.dp),
        )
        FilterChips(
            selected = filter,
            counts = state.filterCounts,
            onSelect = onFilterChange,
            modifier = Modifier.padding(top = 16.dp),
        )
        Column(
            modifier = sectionPadding.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (visibleClients.isEmpty()) {
                EmptyResults(query = query)
            } else {
                visibleClients.forEach { client ->
                    ClientRowCard(client = client, onClick = { onClientClick(client) })
                }
            }
        }
        TipBanner(modifier = sectionPadding.padding(top = 16.dp))
    }
}

// region Header + KPIs (O9)

@Composable
private fun DirectoryHeader(total: Int, onNewClient: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.clients_title),
                    style = MaterialTheme.typography.headlineSmall.copy(letterSpacing = (-0.33).sp),
                    color = VuelveOnSurface,
                    modifier = Modifier.alignByBaseline(),
                )
                Text(
                    text = stringResource(R.string.clients_registered, total),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = VuelveOnSurfaceVariant,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            Text(
                text = stringResource(R.string.clients_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = VuelveOnSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        val newDescription = stringResource(R.string.clients_new_description)
        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .height(40.dp)
                .subtleShadow(CircleShape)
                .clip(CircleShape)
                .background(VuelveBlue600)
                .clickable(onClick = onNewClient, role = Role.Button, onClickLabel = newDescription)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Image(painter = painterResource(R.drawable.ic_clients_person_add), contentDescription = null)
            Text(
                text = stringResource(R.string.clients_new),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun RowScope.KpiCard(
    label: String,
    value: String,
    @DrawableRes icon: Int,
    iconTint: Color,
    circleColor: Color,
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .background(VuelveSurfaceContainerLow, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = VuelveOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.18).sp),
                color = VuelveOnSurface,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(36.dp)
                .background(circleColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(icon), contentDescription = null, tint = iconTint)
        }
    }
}

// endregion

// region Search + filters (M1, M12)

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = VuelveOnSurface)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(VuelvePrimary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .subtleShadow(shape)
                    .background(Color.White, shape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(painter = painterResource(R.drawable.ic_clients_search), contentDescription = null)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.clients_search_placeholder),
                            style = textStyle,
                            color = VuelveOutline,
                            maxLines = 1,
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = { onQueryChange("") }, role = Role.Button),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_clients_close),
                            contentDescription = stringResource(R.string.clients_search_clear),
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun FilterChips(
    selected: ClientFilter,
    counts: Map<ClientFilter, Int>,
    onSelect: (ClientFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ClientFilter.entries.forEach { option ->
            val count = counts[option] ?: 0
            val label = when (option) {
                ClientFilter.All -> stringResource(R.string.clients_filter_all, count)
                ClientFilter.ToContact -> stringResource(R.string.clients_filter_to_contact, count)
                ClientFilter.Inactive -> stringResource(R.string.clients_filter_inactive, count)
                ClientFilter.UpToDate -> stringResource(R.string.clients_filter_up_to_date, count)
            }
            val dotColor = when (option) {
                ClientFilter.ToContact -> StatusOverdueDot
                ClientFilter.UpToDate -> StatusUpToDateDot
                else -> null
            }
            FilterChip(
                label = label,
                selected = option == selected,
                dotColor = dotColor,
                onClick = { onSelect(option) },
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, dotColor: Color?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .then(if (selected) Modifier.subtleShadow(CircleShape) else Modifier)
            .clip(CircleShape)
            .background(if (selected) VuelvePrimaryContainer else VuelveSurfaceContainerLow)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        when {
            selected -> Image(painter = painterResource(R.drawable.ic_clients_check), contentDescription = null)
            dotColor != null -> Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) VuelveOnPrimaryContainer else VuelveOnSurface,
            maxLines = 1,
        )
    }
}

// endregion

// region Client rows (M8)

@Composable
private fun ClientRowCard(client: ClientRow, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow(shape)
            .clip(shape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                InitialsAvatar(client = client)
                Column {
                    Text(
                        text = client.ownerName,
                        style = MaterialTheme.typography.titleMedium,
                        color = VuelveOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = client.petName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = VuelvePrimary,
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
            Column(
                modifier = Modifier.padding(start = 8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ClientStatusBadge(status = client.status, overdueDays = client.overdueDays)
                SuggestedDate(date = client.suggestedDate)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_clients_history), contentDescription = null)
                Text(
                    text = client.lastVisitSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = VuelveOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Image(
                painter = painterResource(R.drawable.ic_clients_chevron_right),
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/** Initials circle with the paw mini-badge colored by retention status (README "Mini-badge de Mascota"). */
@Composable
private fun InitialsAvatar(client: ClientRow) {
    Box {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(client.avatarTone.container, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = client.initials,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = client.avatarTone.content,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 4.dp, y = 4.dp)
                .size(20.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_clients_paw_badge),
                contentDescription = null,
                tint = when (client.status) {
                    ClientStatus.Overdue -> StatusOverdueDot
                    ClientStatus.UpToDate -> StatusUpToDateDot
                    else -> VuelvePrimary
                },
            )
        }
    }
}

@Composable
private fun ClientStatusBadge(status: ClientStatus, overdueDays: Int) {
    val (container, content) = when (status) {
        ClientStatus.ToContact -> StatusContactContainer to StatusDueText
        ClientStatus.Overdue -> StatusOverdueContainer to StatusOverdueText
        ClientStatus.FollowUpToday -> VuelveBlue600 to Color.White
        ClientStatus.UpToDate -> StatusUpToDateContainer to StatusUpToDateText
    }
    val label = when (status) {
        ClientStatus.ToContact -> stringResource(R.string.clients_status_contact)
        ClientStatus.Overdue -> pluralStringResource(R.plurals.clients_status_overdue, overdueDays, overdueDays)
        ClientStatus.FollowUpToday -> stringResource(R.string.clients_status_follow_up_today)
        ClientStatus.UpToDate -> stringResource(R.string.clients_status_up_to_date)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = content,
        maxLines = 1,
        modifier = Modifier
            .background(container, CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
private fun SuggestedDate(date: String?) {
    val prefix = stringResource(R.string.clients_suggested)
    val today = stringResource(R.string.clients_suggested_today)
    val text = buildAnnotatedString {
        append(prefix)
        if (date == null) {
            withStyle(SpanStyle(color = VuelveOnSurface)) { append(today) }
        } else {
            append(date)
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = VuelveOnSurfaceVariant,
        maxLines = 1,
    )
}

@Composable
private fun EmptyResults(query: String) {
    Text(
        text = if (query.isBlank()) {
            stringResource(R.string.clients_empty_filter)
        } else {
            stringResource(R.string.clients_empty, query.trim())
        },
        style = MaterialTheme.typography.bodyMedium,
        color = VuelveOnSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 24.dp),
    )
}

// endregion

// region Tip (O19)

@Composable
private fun TipBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VuelveSurfaceContainer, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(VuelveMint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(painter = painterResource(R.drawable.ic_clients_lightbulb), contentDescription = null)
        }
        Column {
            Text(
                text = stringResource(R.string.clients_tip_title),
                style = MaterialTheme.typography.labelMedium,
                color = VuelveOnSurface,
            )
            Text(
                text = stringResource(R.string.clients_tip_body),
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 24.sp),
                color = VuelveOnSurfaceVariant,
            )
        }
    }
}

// endregion

@Preview(showBackground = true, widthDp = 390, heightDp = 1226)
@Composable
private fun ClientsScreenPreview() {
    TemplateTheme {
        ClientsScreen(state = SampleClientsUiState, onTabSelected = {})
    }
}
