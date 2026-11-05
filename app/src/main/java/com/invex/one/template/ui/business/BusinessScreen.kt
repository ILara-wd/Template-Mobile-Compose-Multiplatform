package com.invex.one.template.ui.business

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.invex.one.template.ui.theme.VuelveMint
import com.invex.one.template.ui.theme.VuelveMintSoft
import com.invex.one.template.ui.theme.VuelveMintStrong
import com.invex.one.template.ui.theme.VuelveOnBlue
import com.invex.one.template.ui.theme.VuelveOnMint
import com.invex.one.template.ui.theme.VuelveOnMintStrong
import com.invex.one.template.ui.theme.VuelveOnPrimaryContainer
import com.invex.one.template.ui.theme.VuelveOnSurface
import com.invex.one.template.ui.theme.VuelveOnSurfaceVariant
import com.invex.one.template.ui.theme.VuelveOutline
import com.invex.one.template.ui.theme.VuelveOutlineSoft
import com.invex.one.template.ui.theme.VuelvePrimary
import com.invex.one.template.ui.theme.VuelvePrimaryContainer
import com.invex.one.template.ui.theme.VuelveSuccess
import com.invex.one.template.ui.theme.VuelveSurfaceContainer
import com.invex.one.template.ui.theme.VuelveSurfaceContainerHigh
import com.invex.one.template.ui.theme.VuelveSurfaceContainerLow
import com.invex.one.template.ui.theme.VuelveWhatsAppDeep

// region Model

data class ServiceRule(
    val name: String,
    val interval: String,
    @DrawableRes val icon: Int,
    val tileColor: Color,
)

data class RevenueBreakdown(
    val label: String,
    val value: String,
    val valueColor: Color,
    val unit: String,
    val footnote: String,
    val footnoteColor: Color,
)

data class BusinessUiState(
    val businessName: String,
    val ownerName: String,
    val ownerRole: String,
    @DrawableRes val photo: Int,
    val reminderPhone: String,
    val location: String,
    val hours: String,
    val monthName: String,
    val revenue: String,
    val revenueGrowth: String,
    val reactivationRevenue: String,
    val reactivatedShare: Int,
    val breakdown: List<RevenueBreakdown>,
    val serviceRules: List<ServiceRule>,
    val sendWindow: String,
    val fasterResponse: Int,
    /** WhatsApp template; `{placeholders}` are highlighted. */
    val template: String,
    val activeServices: Int,
    val priceFrom: String,
)

/** Sample data from the Figma frame until the screen is backed by a repository. */
val SampleBusinessUiState = BusinessUiState(
    businessName = "Patitas Felices Grooming",
    ownerName = "Ana Martínez",
    ownerRole = "Propietaria",
    photo = R.drawable.img_sample_business,
    reminderPhone = "+52 55 1234 5678",
    location = "Col. Del Valle, CDMX • Local y A Domicilio",
    hours = "Lun a Sáb • 9:00 am - 6:00 pm",
    monthName = "Octubre",
    revenue = "$28,450",
    revenueGrowth = "+18%",
    reactivationRevenue = "$4,320",
    reactivatedShare = 33,
    breakdown = listOf(
        RevenueBreakdown("Por retención", "$9,360", VuelveWhatsAppDeep, "33% total", "Recuperados", VuelveSuccess),
        RevenueBreakdown("Ticket Promedio", "$475", VuelveOnSurface, "MXN / mascota", "+5% vs sep", VuelvePrimary),
        RevenueBreakdown("Reactivadas", "24", VuelvePrimary, "visitas mes", "92% asistencia", VuelveWhatsAppDeep),
    ),
    serviceRules = listOf(
        ServiceRule("Baño y corte regular", "Cada 4 semanas (28 días)", R.drawable.ic_business_scissors, VuelvePrimaryContainer),
        ServiceRule("Solo baño higiénico", "Cada 2 semanas (14 días)", R.drawable.ic_business_bath, VuelveSurfaceContainerHigh),
        ServiceRule("Tratamiento o deslanado", "Cada 6 semanas (42 días)", R.drawable.ic_business_paw_service, VuelveMintSoft),
    ),
    sendWindow = "10:00 AM y 1:00 PM",
    fasterResponse = 42,
    template = "\"Hola {nombre_tutor}, ¡toca consentir a {nombre_mascota}! 🐾 Ya pasaron 4 semanas de su " +
        "último corte y queremos que siga fresco y cómodo. ¿Te apartamos cita para este viernes o sábado?\"",
    activeServices = 4,
    priceFrom = "$350 MXN",
)

// endregion

/** "Mi Negocio" business profile (Figma 95:3425). Opened from the account avatar of the main tabs. */
@Composable
fun BusinessScreen(
    state: BusinessUiState,
    onBack: () -> Unit,
    onSettings: () -> Unit = {},
    onChangePhoto: () -> Unit = {},
    onEdit: () -> Unit = {},
    onShare: () -> Unit = {},
    onModifyRule: (ServiceRule) -> Unit = {},
    onCustomizeTemplate: () -> Unit = {},
    onCatalog: () -> Unit = {},
    onDownloadReport: () -> Unit = {},
    onSupport: () -> Unit = {},
) {
    var currentPeriod by rememberSaveable { mutableStateOf(true) }
    var autoReminders by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        containerColor = VuelveBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            VuelveDetailTopBar(
                title = stringResource(R.string.business_title),
                accountAvatar = painterResource(R.drawable.img_sample_profile),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
        ) {
            Header(onSettings = onSettings)
            ProfileHero(
                state = state,
                onChangePhoto = onChangePhoto,
                onEdit = onEdit,
                onShare = onShare,
                modifier = Modifier.padding(top = 8.dp),
            )
            BalanceSection(
                state = state,
                currentPeriod = currentPeriod,
                onPeriodChange = { currentPeriod = it },
                modifier = Modifier.padding(top = 24.dp),
            )
            RulesSection(
                state = state,
                autoReminders = autoReminders,
                onAutoRemindersChange = { autoReminders = it },
                onModifyRule = onModifyRule,
                onCustomizeTemplate = onCustomizeTemplate,
                modifier = Modifier.padding(top = 24.dp),
            )
            OperationsSection(
                state = state,
                onCatalog = onCatalog,
                onDownloadReport = onDownloadReport,
                onSupport = onSupport,
                modifier = Modifier.padding(top = 24.dp),
            )
            Footer(businessName = state.businessName, modifier = Modifier.padding(top = 32.dp))
        }
    }
}

// region Header + profile

@Composable
private fun Header(onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.business_heading),
                    style = MaterialTheme.typography.headlineSmall.copy(letterSpacing = (-0.33).sp),
                    color = VuelveOnSurface,
                )
                Row(
                    modifier = Modifier
                        .background(VuelveMint, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Image(painter = painterResource(R.drawable.ic_business_pro_badge), contentDescription = null)
                    Text(
                        text = stringResource(R.string.business_plan_active),
                        style = MaterialTheme.typography.labelSmall,
                        color = VuelveOnMint,
                        maxLines = 1,
                    )
                }
            }
            Text(
                text = stringResource(R.string.business_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = VuelveOnSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(VuelveSurfaceContainerHigh)
                .clickable(onClick = onSettings, role = Role.Button),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_business_settings),
                contentDescription = stringResource(R.string.business_settings),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileHero(
    state: BusinessUiState,
    onChangePhoto: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(Color.White, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box {
                Image(
                    painter = painterResource(state.photo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(VuelveSurfaceContainer),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(28.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(VuelvePrimary)
                        .clickable(onClick = onChangePhoto, role = Role.Button),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_business_camera),
                        contentDescription = stringResource(R.string.business_change_photo),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = state.businessName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.18).sp),
                        color = VuelveOnSurface,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_business_verified_blue),
                        contentDescription = stringResource(R.string.business_verified),
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Image(painter = painterResource(R.drawable.ic_business_person_small), contentDescription = null)
                    Text(
                        text = state.ownerName,
                        style = MaterialTheme.typography.labelMedium,
                        color = VuelveOnSurfaceVariant,
                    )
                    Text(text = "•", style = MaterialTheme.typography.labelMedium, color = VuelveOutlineSoft)
                    Text(
                        text = state.ownerRole,
                        style = MaterialTheme.typography.labelMedium,
                        color = VuelveOnSurfaceVariant,
                    )
                }
                Row(
                    modifier = Modifier
                        .background(VuelveMint.copy(alpha = 0.3f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(VuelveSuccess, CircleShape),
                    )
                    Text(
                        text = stringResource(R.string.business_whatsapp_connected),
                        style = MaterialTheme.typography.labelSmall,
                        color = VuelveOnMint,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VuelveSurfaceContainerLow, RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ContactLine(icon = R.drawable.ic_login_phone, iconTint = Color.Unspecified) {
                Text(
                    text = state.reminderPhone,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = VuelveOnSurface,
                )
                Text(
                    text = stringResource(R.string.business_reminder_line),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = VuelveOutlineSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ContactLine(icon = R.drawable.ic_business_location, iconTint = Color.Unspecified) {
                Text(
                    text = state.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = VuelveOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ContactLine(icon = R.drawable.ic_detail_schedule, iconTint = VuelveOutline) {
                Text(
                    text = state.hours,
                    style = MaterialTheme.typography.bodySmall,
                    color = VuelveOnSurfaceVariant,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VuelveSurfaceContainer)
                    .clickable(onClick = onEdit, role = Role.Button)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            ) {
                Image(painter = painterResource(R.drawable.ic_business_edit_small), contentDescription = null)
                Text(
                    text = stringResource(R.string.business_edit),
                    style = MaterialTheme.typography.labelMedium,
                    color = VuelveOnSurface,
                    maxLines = 1,
                )
            }
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VuelveSurfaceContainerLow)
                    .clickable(onClick = onShare, role = Role.Button)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_business_share),
                    contentDescription = stringResource(R.string.business_share),
                )
            }
        }
    }
}

@Composable
private fun ContactLine(@DrawableRes icon: Int, iconTint: Color, content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.width(14.dp), contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(icon), contentDescription = null, tint = iconTint)
        }
        content()
    }
}

// endregion

// region Balance

@Composable
private fun SectionTitle(title: String, subtitle: String?, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = VuelveOnSurface)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = VuelveOnSurfaceVariant)
            }
        }
        Box(modifier = Modifier.padding(start = 8.dp)) { trailing() }
    }
}

@Composable
private fun BalanceSection(
    state: BusinessUiState,
    currentPeriod: Boolean,
    onPeriodChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(
            title = stringResource(R.string.business_balance_title),
            subtitle = stringResource(R.string.business_balance_subtitle, state.monthName),
        ) {
            Row(
                modifier = Modifier
                    .background(VuelveSurfaceContainer, CircleShape)
                    .padding(2.dp)
                    .selectableGroup(),
            ) {
                PeriodOption(stringResource(R.string.business_period_current), currentPeriod) { onPeriodChange(true) }
                PeriodOption(stringResource(R.string.business_period_previous), !currentPeriod) { onPeriodChange(false) }
            }
        }
        RevenueHero(state = state)
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            state.breakdown.forEach { item -> BreakdownCard(item) }
        }
    }
}

@Composable
private fun PeriodOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = if (selected) VuelveOnSurface else VuelveOnSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Color.White else Color.Transparent)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun RevenueHero(state: BusinessUiState) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.05f))
            .clip(shape)
            .background(Brush.linearGradient(listOf(VuelveBlue600, VuelvePrimary))),
    ) {
        // Decorative circle peeking from the bottom-right corner.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 24.dp, y = 24.dp)
                .size(128.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape),
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.business_revenue_overline),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.6.sp),
                    color = VuelveOnBlue,
                )
                Row(
                    modifier = Modifier
                        .background(VuelveMintStrong, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Image(painter = painterResource(R.drawable.ic_business_trend_up_dark), contentDescription = null)
                    Text(
                        text = state.revenueGrowth,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = VuelveOnMintStrong,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = state.revenue,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    modifier = Modifier.alignByBaseline(),
                )
                Text(
                    text = stringResource(R.string.detail_currency),
                    style = MaterialTheme.typography.labelLarge,
                    color = VuelveOnBlue,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_business_sparkle_light), contentDescription = null)
                Text(
                    text = stringResource(R.string.business_revenue_reactivations, state.reactivationRevenue),
                    style = MaterialTheme.typography.bodySmall,
                    color = VuelveOnBlue.copy(alpha = 0.9f),
                )
            }
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val footStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.5.sp, letterSpacing = 0.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.business_revenue_reactivated_share, state.reactivatedShare),
                        style = footStyle,
                        color = VuelveOnBlue,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        text = stringResource(R.string.business_revenue_organic_share, 100 - state.reactivatedShare),
                        style = footStyle,
                        color = VuelveOnBlue,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                val share = state.reactivatedShare.coerceIn(0, 100) / 100f
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                ) {
                    if (share > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(share)
                                .fillMaxHeight()
                                .background(VuelveMintStrong),
                        )
                    }
                    if (share < 1f) {
                        Box(
                            modifier = Modifier
                                .weight(1f - share)
                                .fillMaxHeight()
                                .background(Color.White.copy(alpha = 0.7f)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.BreakdownCard(item: RevenueBreakdown) {
    Column(
        modifier = Modifier
            .weight(1f)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(8.dp),
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = VuelveOnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Column(
            modifier = Modifier.padding(top = 4.dp, bottom = 7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text = item.value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = item.valueColor,
            )
            Text(
                text = item.unit,
                style = MaterialTheme.typography.labelSmall,
                color = VuelveOnSurfaceVariant,
                maxLines = 1,
            )
        }
        Text(
            text = item.footnote,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
            ),
            color = item.footnoteColor,
            maxLines = 1,
        )
    }
}

// endregion

// region Retention rules

@Composable
private fun RulesSection(
    state: BusinessUiState,
    autoReminders: Boolean,
    onAutoRemindersChange: (Boolean) -> Unit,
    onModifyRule: (ServiceRule) -> Unit,
    onCustomizeTemplate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(
            title = stringResource(R.string.business_rules_title),
            subtitle = stringResource(R.string.business_rules_subtitle),
        ) {
            Image(painter = painterResource(R.drawable.ic_business_robot), contentDescription = null)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .toggleable(value = autoReminders, onValueChange = onAutoRemindersChange, role = Role.Switch)
                .padding(16.dp),
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
                        .size(40.dp)
                        .background(VuelveMint.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painter = painterResource(R.drawable.ic_clients_bell), contentDescription = null)
                }
                Column {
                    Text(
                        text = stringResource(R.string.business_auto_reminders),
                        style = MaterialTheme.typography.labelLarge,
                        color = VuelveOnSurface,
                    )
                    Text(
                        text = stringResource(R.string.business_auto_reminders_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = VuelveOnSurfaceVariant,
                    )
                }
            }
            VuelveSwitch(checked = autoReminders, modifier = Modifier.padding(start = 8.dp))
        }

        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.business_frequency_overline),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
                color = VuelveOnSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 11.dp, bottom = 7.dp),
            )
            state.serviceRules.forEach { rule ->
                ServiceRuleRow(rule = rule, onModify = { onModifyRule(rule) })
            }
        }

        SendWindowCard(
            state = state,
            onCustomizeTemplate = onCustomizeTemplate,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** Visual-only switch (44x24, green when on); the parent row handles the toggle semantics. */
@Composable
private fun VuelveSwitch(checked: Boolean, modifier: Modifier = Modifier) {
    val thumbOffset by animateDpAsState(targetValue = if (checked) 22.dp else 2.dp, label = "switchThumb")
    Box(
        modifier = modifier
            .width(44.dp)
            .height(24.dp)
            .background(if (checked) VuelveSuccess else VuelveOutlineSoft, CircleShape),
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset, y = 2.dp)
                .size(20.dp)
                .background(Color.White, CircleShape),
        )
    }
}

@Composable
private fun ServiceRuleRow(rule: ServiceRule, onModify: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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
                    .background(rule.tileColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(painter = painterResource(rule.icon), contentDescription = null)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = rule.name, style = MaterialTheme.typography.labelMedium, color = VuelveOnSurface)
                Text(text = rule.interval, style = MaterialTheme.typography.bodySmall, color = VuelveOnSurfaceVariant)
            }
        }
        Text(
            text = stringResource(R.string.business_modify),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = VuelvePrimary,
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(VuelveSurfaceContainer)
                .clickable(onClick = onModify, role = Role.Button)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

private val PlaceholderRegex = Regex("\\{[a-z_]+\\}")

/** Highlights `{placeholder}` tokens like the Figma template preview. */
private fun templateText(template: String): AnnotatedString = buildAnnotatedString {
    var last = 0
    PlaceholderRegex.findAll(template).forEach { match ->
        append(template.substring(last, match.range.first))
        withStyle(
            SpanStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = VuelveOnPrimaryContainer,
                background = VuelvePrimaryContainer,
            ),
        ) { append(match.value) }
        last = match.range.last + 1
    }
    append(template.substring(last))
}

@Composable
private fun SendWindowCard(state: BusinessUiState, onCustomizeTemplate: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_home_followups_btn),
                    contentDescription = null,
                    tint = VuelvePrimary,
                )
                Text(
                    text = stringResource(R.string.business_send_window),
                    style = MaterialTheme.typography.labelMedium,
                    color = VuelveOnSurface,
                )
            }
            Text(
                text = stringResource(R.string.business_best_conversion),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = VuelveOnMint,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(VuelveMint, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        val body = stringResource(R.string.business_send_window_body, state.sendWindow, state.fasterResponse)
        Text(
            text = buildAnnotatedString {
                val start = body.indexOf(state.sendWindow)
                if (start < 0) {
                    append(body)
                } else {
                    append(body.substring(0, start))
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = VuelveOnSurface)) {
                        append(state.sendWindow)
                    }
                    append(body.substring(start + state.sendWindow.length))
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = VuelveOnSurfaceVariant,
        )
        Column(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .background(VuelveSurfaceContainerLow, RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Image(painter = painterResource(R.drawable.ic_business_whatsapp_template), contentDescription = null)
                    Text(
                        text = stringResource(R.string.business_template_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = VuelveOnSurfaceVariant,
                    )
                }
                Text(
                    text = stringResource(R.string.business_template_customize),
                    style = MaterialTheme.typography.labelSmall,
                    color = VuelvePrimary,
                    modifier = Modifier.clickable(onClick = onCustomizeTemplate, role = Role.Button),
                )
            }
            Text(
                text = templateText(state.template),
                style = MaterialTheme.typography.bodySmall,
                color = VuelveOnSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .padding(8.dp),
            )
        }
    }
}

// endregion

// region Operations + footer

@Composable
private fun OperationsSection(
    state: BusinessUiState,
    onCatalog: () -> Unit,
    onDownloadReport: () -> Unit,
    onSupport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.business_ops_title),
            style = MaterialTheme.typography.titleMedium,
            color = VuelveOnSurface,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
        OperationRow(
            icon = R.drawable.ic_business_catalog,
            circle = VuelveSurfaceContainer,
            title = stringResource(R.string.business_catalog),
            subtitle = stringResource(R.string.business_catalog_subtitle, state.activeServices, state.priceFrom),
            trailing = R.drawable.ic_clients_chevron_right,
            onClick = onCatalog,
        )
        OperationRow(
            icon = R.drawable.ic_business_download_doc,
            circle = VuelveMint.copy(alpha = 0.3f),
            title = stringResource(R.string.business_report),
            subtitle = stringResource(R.string.business_report_subtitle),
            trailing = R.drawable.ic_business_download,
            onClick = onDownloadReport,
        )
        OperationRow(
            icon = R.drawable.ic_business_support,
            circle = VuelveSurfaceContainerHigh,
            title = stringResource(R.string.business_support),
            subtitle = stringResource(R.string.business_support_subtitle),
            trailing = R.drawable.ic_business_chat_green,
            onClick = onSupport,
        )
    }
}

@Composable
private fun OperationRow(
    @DrawableRes icon: Int,
    circle: Color,
    title: String,
    subtitle: String,
    @DrawableRes trailing: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(circle, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(painter = painterResource(icon), contentDescription = null)
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = VuelveOnSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = VuelveOnSurfaceVariant)
            }
        }
        Image(
            painter = painterResource(trailing),
            contentDescription = null,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun Footer(businessName: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(0.7f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(painter = painterResource(R.drawable.ic_business_shield), contentDescription = null)
            Text(
                text = stringResource(R.string.business_footer_version, businessName),
                style = MaterialTheme.typography.labelSmall,
                color = VuelveOnSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = stringResource(R.string.business_footer_sync),
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

// endregion

@Preview(showBackground = true, widthDp = 390, heightDp = 1800)
@Composable
private fun BusinessScreenPreview() {
    TemplateTheme {
        BusinessScreen(state = SampleBusinessUiState, onBack = {})
    }
}
