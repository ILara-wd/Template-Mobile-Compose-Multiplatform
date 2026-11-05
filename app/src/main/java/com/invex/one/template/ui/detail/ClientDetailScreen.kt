package com.invex.one.template.ui.detail

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.template.R
import com.invex.one.template.ui.components.VuelveDetailTopBar
import com.invex.one.template.ui.components.subtleShadow
import com.invex.one.template.ui.theme.TemplateTheme
import com.invex.one.template.ui.theme.VuelveBackground
import com.invex.one.template.ui.theme.VuelveBlue600
import com.invex.one.template.ui.theme.VuelveMintPale
import com.invex.one.template.ui.theme.VuelveMintStrong
import com.invex.one.template.ui.theme.VuelveOnPrimaryContainer
import com.invex.one.template.ui.theme.VuelveOnSuccessContainer
import com.invex.one.template.ui.theme.VuelveOnSurface
import com.invex.one.template.ui.theme.VuelveOnSurfaceVariant
import com.invex.one.template.ui.theme.VuelvePrimary
import com.invex.one.template.ui.theme.VuelvePrimaryContainer
import com.invex.one.template.ui.theme.VuelveSecondaryContainer
import com.invex.one.template.ui.theme.VuelveSuccess
import com.invex.one.template.ui.theme.VuelveSurfaceContainer
import com.invex.one.template.ui.theme.VuelveSurfaceContainerHigh
import com.invex.one.template.ui.theme.VuelveSurfaceContainerLow
import com.invex.one.template.ui.theme.VuelveWhatsApp
import com.invex.one.template.ui.theme.FollowUpCardShadow

// region Model

/** Note icon Figma pairs with each visit in the timeline. */
enum class VisitNoteIcon(@DrawableRes val res: Int) {
    Note(R.drawable.ic_detail_note_margin),
    Medical(R.drawable.ic_detail_medical_margin),
    Grooming(R.drawable.ic_detail_scissors_margin),
}

data class Visit(
    val date: String,
    val service: String,
    val price: String,
    val note: String,
    val noteIcon: VisitNoteIcon,
)

data class ClientDetailUiState(
    val id: String,
    val ownerName: String,
    val isVip: Boolean,
    val petName: String,
    val breed: String,
    val petAge: String,
    val phone: String,
    val contactToday: Boolean,
    val elapsedSinceLastService: String,
    val lastServiceName: String,
    val usualFrequencyWord: String,
    val lastContact: String,
    val lastContactNote: String,
    val usualFrequencyRange: String,
    val frequencyNote: String,
    val totalVisits: Int,
    val visitsSince: String,
    val visits: List<Visit>,
) {
    val initials: String
        get() = ownerName.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
}

/** Sample data from the Figma frame (María López / Luna). */
val SampleClientDetail = ClientDetailUiState(
    id = "maria-luna",
    ownerName = "María López",
    isVip = true,
    petName = "Luna",
    breed = "Cocker Spaniel",
    petAge = "3 años",
    phone = "+52 55 4192 8834",
    contactToday = true,
    elapsedSinceLastService = "4 semanas",
    lastServiceName = "baño y corte",
    usualFrequencyWord = "mensual",
    lastContact = "Hace 28 días",
    lastContactNote = " (Agendó visita anterior con éxito)",
    usualFrequencyRange = "28 a 30 días",
    frequencyNote = "Cliente constante",
    totalVisits = 7,
    visitsSince = "Desde hace 6 meses",
    visits = listOf(
        Visit("26 Sep 2023", "Baño y corte completo", "480", "Se portó muy bien. Uñas recortadas y perfume hipoalergénico.", VisitNoteIcon.Note),
        Visit("28 Ago 2023", "Baño desparasitante", "350", "Tratamiento preventivo para pulgas.", VisitNoteIcon.Medical),
        Visit("25 Jul 2023", "Baño y corte", "480", "Corte estándar estilo cachorro.", VisitNoteIcon.Grooming),
    ),
)

/** Detail data is only available for the Figma sample client for now. */
fun sampleClientDetail(clientId: String): ClientDetailUiState? =
    SampleClientDetail.takeIf { it.id == clientId }

// endregion

/** "Detalle del cliente — María y Luna" (Figma 93:3217). Pushed over the tabs, with a sticky action bar. */
@Composable
fun ClientDetailScreen(
    state: ClientDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit = {},
    onMoreOptions: () -> Unit = {},
    onCall: () -> Unit = {},
    onWhatsApp: () -> Unit = {},
    onRegisterVisit: () -> Unit = {},
) {
    Scaffold(
        containerColor = VuelveBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            VuelveDetailTopBar(
                title = stringResource(R.string.detail_title),
                accountAvatar = painterResource(R.drawable.img_sample_profile),
                onBack = onBack,
            )
        },
        bottomBar = { StickyActions(onNewVisit = onRegisterVisit, onWhatsApp = onWhatsApp) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            ProfileCard(state = state, onEdit = onEdit, onMoreOptions = onMoreOptions, onCall = onCall)
            if (state.contactToday) {
                RetentionCard(state = state, onWhatsApp = onWhatsApp, modifier = Modifier.padding(top = 16.dp))
            }
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetricCard(
                    icon = R.drawable.ic_detail_frequency,
                    label = stringResource(R.string.detail_frequency_label),
                    value = state.usualFrequencyRange,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_vuelve_check_circle_small),
                            contentDescription = null,
                        )
                        Text(
                            text = state.frequencyNote,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = VuelveSuccess,
                        )
                    }
                }
                MetricCard(
                    icon = R.drawable.ic_detail_visits,
                    label = stringResource(R.string.detail_visits_label),
                    value = pluralStringResource(R.plurals.detail_visits_value, state.totalVisits, state.totalVisits),
                ) {
                    Text(
                        text = state.visitsSince,
                        style = MaterialTheme.typography.bodySmall,
                        color = VuelveOnSurfaceVariant,
                    )
                }
            }
            VisitHistory(
                visits = state.visits,
                onRegisterVisit = onRegisterVisit,
                modifier = Modifier.padding(top = 20.dp),
            )
        }
    }
}

// region Profile (O13)

@Composable
private fun ProfileCard(
    state: ClientDetailUiState,
    onEdit: () -> Unit,
    onMoreOptions: () -> Unit,
    onCall: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(Color.White, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(VuelvePrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.initials,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.33).sp,
                            ),
                            color = VuelveOnPrimaryContainer,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(24.dp)
                            .subtleShadow(CircleShape)
                            .background(VuelveSuccess, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_vuelve_paw_small),
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = state.ownerName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = VuelveOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (state.isVip) {
                            Text(
                                text = stringResource(R.string.detail_vip),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    lineHeight = 15.sp,
                                    letterSpacing = 0.sp,
                                ),
                                color = VuelveOnSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier
                                    .background(VuelveSurfaceContainerHigh, CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Image(painter = painterResource(R.drawable.ic_detail_pet_icon), contentDescription = null)
                        Text(
                            text = state.petName,
                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.sp),
                            color = VuelvePrimary,
                        )
                        Text(
                            text = stringResource(R.string.detail_pet_age, state.breed, state.petAge),
                            style = MaterialTheme.typography.bodySmall,
                            color = VuelveOnSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VuelveSurfaceContainer)
                        .clickable(onClick = onEdit, role = Role.Button)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Image(painter = painterResource(R.drawable.ic_detail_edit), contentDescription = null)
                    Text(
                        text = stringResource(R.string.detail_edit),
                        style = MaterialTheme.typography.labelMedium,
                        color = VuelveOnSurface,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onMoreOptions, role = Role.Button),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_detail_more_vert),
                        contentDescription = stringResource(R.string.detail_more_options),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VuelveSurfaceContainerLow, RoundedCornerShape(8.dp))
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
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
                        .size(32.dp)
                        .background(VuelveBackground, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painter = painterResource(R.drawable.ic_detail_phone), contentDescription = null)
                }
                Column {
                    Text(
                        text = stringResource(R.string.detail_phone_label),
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 11.sp),
                        color = VuelveOnSurfaceVariant,
                    )
                    Text(
                        text = state.phone,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        color = VuelveOnSurface,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(32.dp)
                    .subtleShadow(CircleShape)
                    .clip(CircleShape)
                    .background(VuelveBackground)
                    .clickable(onClick = onCall, role = Role.Button)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_detail_call), contentDescription = null)
                Text(
                    text = stringResource(R.string.detail_call),
                    style = MaterialTheme.typography.labelMedium,
                    color = VuelveOnSurface,
                )
            }
        }
    }
}

// endregion

// region Retention status (O14)

/** Returns [template] with every occurrence of [highlight] styled with [style]. */
private fun highlighted(template: String, highlight: String, style: SpanStyle): AnnotatedString =
    buildAnnotatedString {
        val start = template.indexOf(highlight)
        if (start < 0 || highlight.isEmpty()) {
            append(template)
        } else {
            append(template.substring(0, start))
            withStyle(style) { append(highlight) }
            append(template.substring(start + highlight.length))
        }
    }

@Composable
private fun RetentionCard(state: ClientDetailUiState, onWhatsApp: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(Color.White, shape)
            .background(VuelvePrimaryContainer.copy(alpha = 0.25f), shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .subtleShadow(CircleShape)
                    .background(VuelveBlue600, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(VuelveMintPale, CircleShape),
                )
                Text(
                    text = stringResource(R.string.detail_contact_today),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.3.sp),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_detail_schedule), contentDescription = null)
                Text(
                    text = stringResource(R.string.detail_on_time_frequency),
                    style = MaterialTheme.typography.labelSmall,
                    color = VuelvePrimary,
                    maxLines = 1,
                )
            }
        }

        Column(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(painter = painterResource(R.drawable.ic_detail_event_margin), contentDescription = null)
                Text(
                    text = highlighted(
                        template = stringResource(
                            R.string.detail_retention_summary,
                            state.elapsedSinceLastService,
                            state.lastServiceName,
                            state.usualFrequencyWord,
                        ),
                        highlight = state.elapsedSinceLastService,
                        style = SpanStyle(fontWeight = FontWeight.Bold, color = VuelvePrimary),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = VuelveOnSurface,
                )
            }
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.detail_last_contact_label))
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = VuelveOnSurface)) {
                        append(state.lastContact)
                    }
                    append(state.lastContactNote)
                },
                style = MaterialTheme.typography.bodySmall,
                color = VuelveOnSurfaceVariant,
                modifier = Modifier.padding(start = 28.dp),
            )
        }

        Button(
            onClick = onWhatsApp,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VuelveWhatsApp, contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_detail_whatsapp_logo_24), contentDescription = null)
                Column {
                    Text(
                        text = stringResource(R.string.detail_whatsapp_title),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 17.5.sp),
                        maxLines = 1,
                    )
                    Text(
                        text = stringResource(R.string.detail_whatsapp_subtitle, state.petName),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            lineHeight = 13.75.sp,
                            letterSpacing = 0.sp,
                        ),
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Image(painter = painterResource(R.drawable.ic_vuelve_arrow_forward_md), contentDescription = null)
        }
    }
}

// endregion

// region Metrics (M7)

@Composable
private fun RowScope.MetricCard(
    @DrawableRes icon: Int,
    label: String,
    value: String,
    footnote: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .weight(1f)
            .subtleShadow(shape)
            .background(Color.White, shape)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Image(painter = painterResource(icon), contentDescription = null)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = VuelveOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.18).sp),
            color = VuelveOnSurface,
            maxLines = 1,
        )
        Box(modifier = Modifier.padding(top = 2.dp)) { footnote() }
    }
}

// endregion

// region Visit history (O15, M18)

@Composable
private fun VisitHistory(visits: List<Visit>, onRegisterVisit: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
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
                Text(
                    text = stringResource(R.string.detail_history_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = VuelveOnSurface,
                    maxLines = 1,
                )
                Text(
                    text = pluralStringResource(R.plurals.detail_history_recent, visits.size, visits.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = VuelveOnSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier
                        .background(VuelveSurfaceContainer, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
            Row(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(32.dp)
                    .clip(CircleShape)
                    .background(VuelveSurfaceContainerHigh)
                    .clickable(onClick = onRegisterVisit, role = Role.Button)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_detail_add_small), contentDescription = null)
                Text(
                    text = stringResource(R.string.detail_register_visit),
                    style = MaterialTheme.typography.labelMedium,
                    color = VuelvePrimary,
                    maxLines = 1,
                )
            }
        }

        // Timeline: 2dp rail at x = 11dp, cards start at 24dp, node dots centered on the rail.
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .drawBehind {
                    val x = 12.dp.toPx()
                    drawLine(
                        color = VuelveSecondaryContainer,
                        start = Offset(x, 12.dp.toPx()),
                        end = Offset(x, size.height - 12.dp.toPx()),
                        strokeWidth = 2.dp.toPx(),
                    )
                }
                .padding(start = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            visits.forEachIndexed { index, visit ->
                VisitCard(visit = visit, isLatest = index == 0)
            }
        }
    }
}

@Composable
private fun VisitCard(visit: Visit, isLatest: Boolean) {
    val shape = RoundedCornerShape(12.dp)
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .subtleShadow(shape)
                .background(Color.White, shape)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(if (isLatest) 2.dp else 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = visit.date,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isLatest) FontWeight.Bold else FontWeight.SemiBold,
                            ),
                            color = if (isLatest) VuelvePrimary else VuelveOnSurfaceVariant,
                        )
                        if (isLatest) {
                            Text(
                                text = stringResource(R.string.detail_latest_badge),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    lineHeight = 15.sp,
                                    letterSpacing = 0.sp,
                                ),
                                color = VuelveOnSuccessContainer,
                                modifier = Modifier
                                    .background(VuelveMintStrong.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Text(
                        text = visit.service,
                        style = MaterialTheme.typography.titleMedium,
                        color = VuelveOnSurface,
                    )
                }
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontSize = if (isLatest) 18.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = VuelveOnSurface,
                            ),
                        ) { append(stringResource(R.string.detail_price, visit.price)) }
                        append(" ")
                        withStyle(SpanStyle(fontSize = 11.sp, color = VuelveOnSurfaceVariant)) {
                            append(stringResource(R.string.detail_currency))
                        }
                    },
                    style = MaterialTheme.typography.titleLarge.copy(letterSpacing = (-0.18).sp),
                    maxLines = 1,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VuelveSurfaceContainerLow, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(painter = painterResource(visit.noteIcon.res), contentDescription = null)
                Text(
                    text = visit.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = VuelveOnSurfaceVariant,
                )
            }
        }
        // Node dot (14dp) centered on the rail with a 4dp background-colored ring.
        Box(
            modifier = Modifier
                .offset(x = (-23).dp, y = 12.dp)
                .size(22.dp)
                .background(VuelveBackground, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .then(if (isLatest) Modifier.shadow(1.dp, CircleShape) else Modifier)
                    .background(if (isLatest) VuelvePrimary else VuelveSecondaryContainer, CircleShape),
            )
        }
    }
}

// endregion

// region Sticky actions (O4)

@Composable
private fun StickyActions(onNewVisit: () -> Unit, onWhatsApp: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, ambientColor = FollowUpCardShadow, spotColor = FollowUpCardShadow)
            .background(VuelveBackground.copy(alpha = 0.95f))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Figma uses a 5-column grid: secondary spans 2, WhatsApp spans 3, 4dp gaps.
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VuelveSurfaceContainerHigh)
                    .clickable(onClick = onNewVisit, role = Role.Button),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            ) {
                Image(painter = painterResource(R.drawable.ic_detail_add_circle), contentDescription = null)
                Text(
                    text = stringResource(R.string.detail_new_visit),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = VuelveOnSurface,
                    maxLines = 1,
                )
            }
            Button(
                onClick = onWhatsApp,
                modifier = Modifier
                    .weight(3f)
                    .height(48.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VuelveWhatsApp, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_detail_whatsapp_logo_20), contentDescription = null)
                Text(
                    text = stringResource(R.string.detail_whatsapp_short),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

// endregion

@Preview(showBackground = true, widthDp = 390, heightDp = 1240)
@Composable
private fun ClientDetailScreenPreview() {
    TemplateTheme {
        ClientDetailScreen(state = SampleClientDetail, onBack = {})
    }
}
