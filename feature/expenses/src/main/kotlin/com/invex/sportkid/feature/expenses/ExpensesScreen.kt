package com.invex.sportkid.feature.expenses

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
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
import com.invex.sportkid.core.designsystem.component.SkChip
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkTextButton
import com.invex.sportkid.core.designsystem.component.SkTonalButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.PaymentStatus
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.label
import com.invex.sportkid.core.ui.labelRes
import com.invex.sportkid.core.ui.weekdayDateLabel
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt
import com.invex.sportkid.core.ui.R as UiR

/**
 * P16. Gastos (plantilla T1, pestaña): periodo, filtros por hijo y deporte, total con barra por
 * deporte y desglose, alerta de pendientes, momentos del mes, historial agrupado por día y
 * acciones para comprobantes y registrar un gasto (O27).
 *
 * "Pagar ahora" marca el gasto como pagado en esta sesión. TODO: pago y repositorio reales.
 */
@Composable
fun ExpensesScreen(
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    athletes: List<ExpenseAthlete> = ExpensesSamples.athletes,
    expensesFor: (YearMonth) -> List<Expense> = { ExpensesSamples.expenses(it, today) },
    moments: List<ExpenseMoment> = ExpensesSamples.moments,
    onRegister: () -> Unit = {},
    onTaxReceipts: () -> Unit = {},
    onExport: () -> Unit = {},
    onReceiptsGallery: () -> Unit = {},
) {
    var month by rememberSaveable { mutableStateOf(YearMonth.from(today)) }
    var athleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var sport by rememberSaveable { mutableStateOf<Sport?>(null) }
    var paidIds by rememberSaveable { mutableStateOf(listOf<String>()) }

    val all = remember(month, paidIds) {
        expensesFor(month).map { if (it.id in paidIds) it.copy(status = PaymentStatus.PAID) else it }
    }
    val visible = all.filter { (athleteId == null || athleteId in it.athleteIds) && (sport == null || it.sport == sport) }
    val names = athletes.associate { it.id to it }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = SportKidTheme.spacing.xl),
    ) {
        PeriodCard(month, today, onMonthChange = { month = it }, modifier = Modifier.padding(horizontal = SportKidTheme.spacing.lg).padding(top = SportKidTheme.spacing.lg))
        Filters(athletes, athleteId, { athleteId = it }, sport, { sport = it }, Modifier.padding(vertical = SportKidTheme.spacing.md))
        Column(Modifier.padding(horizontal = SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
            TotalCard(visible)
            val pending = visible.filter { it.status == PaymentStatus.PENDING }
            if (pending.isNotEmpty()) {
                PendingCard(pending, names, onPay = { paidIds = paidIds + pending.map(Expense::id) })
            }
            if (month == YearMonth.from(today)) {
                val shown = moments.filter { athleteId == null || it.athleteId == athleteId }
                if (shown.isNotEmpty()) Moments(shown, names, onReceiptsGallery, Modifier.padding(top = SportKidTheme.spacing.sm))
            }
            History(visible, names, onExport, Modifier.padding(top = SportKidTheme.spacing.sm))
            Row(Modifier.padding(top = SportKidTheme.spacing.sm), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                SkTonalButton(
                    stringResource(R.string.expenses_tax_receipts),
                    onClick = onTaxReceipts,
                    modifier = Modifier.weight(1f),
                    icon = painterResource(R.drawable.ic_expenses_receipt),
                    size = SkButtonSize.Medium,
                    textStyle = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                )
                SkTonalButton(
                    stringResource(R.string.expenses_register),
                    onClick = onRegister,
                    modifier = Modifier.weight(1f),
                    icon = painterResource(R.drawable.ic_expenses_add),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    size = SkButtonSize.Medium,
                    textStyle = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

/** Periodo activo: mes anterior, nombre del mes y menú "Cambiar" con los últimos 12 meses. */
@Composable
private fun PeriodCard(month: YearMonth, today: LocalDate, onMonthChange: (YearMonth) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    var menu by rememberSaveable { mutableStateOf(false) }
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = colors.surfaceContainerLowest, shadowElevation = SkElevation.card) {
        Row(Modifier.padding(SportKidTheme.spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onMonthChange(month.minusMonths(1)) }) {
                Icon(painterResource(R.drawable.ic_expenses_prev), contentDescription = stringResource(R.string.expenses_prev_month), tint = colors.onSurfaceVariant)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.expenses_period).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
                    color = colors.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(month.label(), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    Icon(painterResource(R.drawable.ic_expenses_calendar), contentDescription = null, tint = colors.primary)
                }
            }
            Box {
                Surface(
                    onClick = { menu = true },
                    shape = CircleShape,
                    color = colors.secondaryContainer,
                    contentColor = colors.onSecondaryContainer,
                    shadowElevation = SkElevation.level1,
                ) {
                    Row(
                        Modifier.padding(horizontal = SportKidTheme.spacing.lg, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(stringResource(R.string.expenses_change), style = MaterialTheme.typography.labelMedium)
                        Icon(painterResource(R.drawable.ic_expenses_dropdown), contentDescription = null)
                    }
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    val current = YearMonth.from(today)
                    (0L until 12L).map { current.minusMonths(it) }.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label(), fontWeight = if (option == month) FontWeight.Bold else null) },
                            onClick = {
                                menu = false
                                onMonthChange(option)
                            },
                        )
                    }
                }
            }
        }
    }
}

/** Filtros: hijos (con su inicial en su color) y deportes (con su punto de color). */
@Composable
private fun Filters(
    athletes: List<ExpenseAthlete>,
    athleteId: String?,
    onAthlete: (String?) -> Unit,
    sport: Sport?,
    onSport: (Sport?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = SportKidTheme.spacing.lg)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        ) {
            SkChip(
                stringResource(R.string.expenses_all_athletes),
                selected = athleteId == null,
                onClick = { onAthlete(null) },
                icon = painterResource(R.drawable.ic_expenses_all),
                height = 28.dp,
                horizontalPadding = SportKidTheme.spacing.lg,
            )
            athletes.forEach { athlete ->
                SkChip(
                    athlete.firstName,
                    selected = athleteId == athlete.id,
                    onClick = { onAthlete(if (athleteId == athlete.id) null else athlete.id) },
                    height = 28.dp,
                    horizontalPadding = SportKidTheme.spacing.lg,
                    leading = {
                        SkAvatar(photo = null, name = athlete.firstName, size = 20.dp, placeholderColor = athlete.color(), placeholderContentColor = Color.White)
                    },
                )
            }
        }
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = SportKidTheme.spacing.lg)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val small = MaterialTheme.typography.labelSmall
            SkChip(
                stringResource(R.string.expenses_all_sports),
                selected = sport == null,
                onClick = { onSport(null) },
                height = 22.dp,
                textStyle = small,
                selectedContainerColor = colors.primaryContainer,
                selectedContentColor = colors.primary,
            )
            Sport.entries.forEach { item ->
                SkChip(
                    stringResource(item.labelRes),
                    selected = sport == item,
                    onClick = { onSport(if (sport == item) null else item) },
                    height = 22.dp,
                    textStyle = small,
                    contentColor = colors.onSurfaceVariant,
                    selectedContainerColor = colors.primaryContainer,
                    selectedContentColor = colors.primary,
                    leading = { Box(Modifier.size(8.dp).background(item.colors().content, CircleShape)) },
                )
            }
        }
    }
}

/** Total del periodo, barra apilada por deporte (de mayor a menor) y desglose con porcentaje. */
@Composable
private fun TotalCard(expenses: List<Expense>) {
    val colors = MaterialTheme.colorScheme
    val total = expenses.sumOf { it.amountPesos }
    val bySport = expenses.groupBy { it.sport }.toList().sortedByDescending { (_, items) -> items.sumOf { it.amountPesos } }
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.surfaceContainerLowest, shadowElevation = SkElevation.card) {
        Box {
            // Halo decorativo en la esquina (Figma: "Ambient Sport Glow" con blur).
            val glow = colors.primary.copy(alpha = 0.08f)
            Box(
                Modifier
                    .matchParentSize()
                    .drawBehind {
                        val radius = 96.dp.toPx()
                        drawCircle(
                            Brush.radialGradient(listOf(glow, Color.Transparent), center = Offset(size.width, 0f), radius = radius),
                            radius = radius,
                            center = Offset(size.width, 0f),
                        )
                    },
            )
            Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.expenses_total).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.6.sp),
                            color = colors.onSurfaceVariant,
                        )
                        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                stringResource(R.string.expenses_amount, total.grouped()),
                                style = MaterialTheme.typography.displaySmall.copy(letterSpacing = (-0.9).sp),
                                color = colors.onSurface,
                            )
                            Text(
                                stringResource(R.string.expenses_currency),
                                modifier = Modifier.padding(bottom = 6.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary,
                            )
                        }
                    }
                    Surface(Modifier.size(48.dp), shape = MaterialTheme.shapes.medium, color = colors.primaryContainer, contentColor = colors.primary, shadowElevation = SkElevation.level1) {
                        Box(contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.ic_expenses_wallet), contentDescription = null) }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(painterResource(R.drawable.ic_expenses_verified), contentDescription = null, tint = SportKidTheme.sportColors.success)
                    Text(pluralStringResource(R.plurals.expenses_count, expenses.size, expenses.size), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                if (total > 0) {
                    Row(
                        Modifier
                            .padding(top = SportKidTheme.spacing.lg)
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceContainer),
                    ) {
                        bySport.forEach { (sport, items) ->
                            Box(Modifier.weight(items.sumOf { it.amountPesos }.toFloat()).fillMaxHeight().background(sport.colors().content))
                        }
                    }
                    Column(Modifier.padding(top = SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        bySport.forEach { (sport, items) -> BreakdownRow(sport, items, total) }
                    }
                }
            }
        }
    }
}

/** Renglón del desglose: punto y nombre del deporte, conceptos, monto y porcentaje del total. */
@Composable
private fun BreakdownRow(sport: Sport, items: List<Expense>, total: Int) {
    val colors = MaterialTheme.colorScheme
    val amount = items.sumOf { it.amountPesos }
    val detail = items.map { it.category }.distinct().sortedBy { it.ordinal }
        .map { stringResource(it.labelRes) }
        .joinToString(stringResource(R.string.expenses_and))
        .replaceFirstChar { it.uppercase() }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(sport.colors().content, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(stringResource(sport.labelRes), style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
        Spacer(Modifier.width(4.dp))
        Text(
            stringResource(R.string.expenses_detail, detail),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            stringResource(R.string.expenses_amount_mxn, amount.grouped()),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurface,
        )
        Text(
            stringResource(R.string.expenses_percent, (amount * 100f / total).roundToInt()),
            modifier = Modifier.width(32.dp),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
    }
}

/** Alerta cálida de pendientes (M3 tonal): monto, concepto y "Pagar ahora". */
@Composable
private fun PendingCard(pending: List<Expense>, athletes: Map<String, ExpenseAthlete>, onPay: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val first = pending.first()
    val who = first.athleteIds.mapNotNull { athletes[it]?.firstName }.joinToString(stringResource(R.string.expenses_and))
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.tertiaryContainer, shadowElevation = SkElevation.card) {
        Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkIconCircle(
                    icon = painterResource(R.drawable.ic_expenses_schedule),
                    contentDescription = null,
                    size = 32.dp,
                    containerColor = colors.tertiary,
                    iconTint = colors.onTertiary,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.expenses_pending_title, stringResource(R.string.expenses_amount_mxn, pending.sumOf { it.amountPesos }.grouped())),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.onTertiaryContainer,
                )
                SkPill(
                    stringResource(R.string.expenses_pending_badge),
                    containerColor = colors.errorContainer,
                    contentColor = colors.onErrorContainer,
                    overline = true,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                )
            }
            val count = pluralStringResource(R.plurals.expenses_pending_count, pending.size, pending.size)
            val text = stringResource(R.string.expenses_pending_detail, count, first.title, who)
            Text(
                buildAnnotatedString {
                    append(text)
                    val start = text.lastIndexOf(who)
                    if (who.isNotEmpty() && start >= 0) addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, start + who.length)
                },
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.tertiary,
            )
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.End) {
                SkButton(
                    stringResource(R.string.expenses_pay_now),
                    onClick = onPay,
                    modifier = Modifier.height(32.dp),
                    leadingIcon = painterResource(R.drawable.ic_expenses_card),
                    size = SkButtonSize.Small,
                    containerColor = colors.tertiary,
                    contentColor = colors.onTertiary,
                )
            }
        }
    }
}

/** "Momentos del mes": fotos de los hijos con su nombre y lugar, y el enlace a la galería de recibos. */
@Composable
private fun Moments(moments: List<ExpenseMoment>, athletes: Map<String, ExpenseAthlete>, onGallery: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionHeader(stringResource(R.string.expenses_moments)) {
            SkTextButton(stringResource(R.string.expenses_receipts_gallery), onClick = onGallery, color = colors.primary, textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
            moments.forEach { moment ->
                val athlete = athletes[moment.athleteId]
                Box(
                    Modifier
                        .weight(1f)
                        .height(112.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceContainer),
                ) {
                    Image(painterResource(moment.photoRes), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Box(Modifier.matchParentSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.7f))))
                    Row(Modifier.align(Alignment.BottomStart).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (athlete != null) Box(Modifier.size(10.dp).background(athlete.color(), CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.expenses_moment_caption, athlete?.firstName.orEmpty(), moment.place),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }
            if (moments.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

/** Título de sección con una acción de texto alineada a la derecha. */
@Composable
private fun SectionHeader(title: String, action: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Box(Modifier.offset(x = SportKidTheme.spacing.lg)) { action() }
    }
}

/** Historial agrupado por día (más reciente primero). */
@Composable
private fun History(expenses: List<Expense>, athletes: Map<String, ExpenseAthlete>, onExport: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionHeader(stringResource(R.string.expenses_history)) {
            SkTextButton(
                stringResource(R.string.expenses_export),
                onClick = onExport,
                color = colors.primary,
                textStyle = MaterialTheme.typography.labelSmall,
                leadingIcon = painterResource(R.drawable.ic_expenses_export),
            )
        }
        if (expenses.isEmpty()) {
            Text(
                stringResource(R.string.expenses_empty),
                modifier = Modifier.padding(vertical = SportKidTheme.spacing.xl),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        expenses.groupBy { it.date }.toSortedMap(compareByDescending { it }).forEach { (date, items) ->
            Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(painterResource(R.drawable.ic_expenses_day), contentDescription = null, tint = colors.onSurfaceVariant)
                    Text(
                        date.weekdayDateLabel().uppercase(),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
                        color = colors.onSurfaceVariant,
                    )
                }
                items.forEach { ExpenseRow(it, athletes) }
            }
        }
    }
}

/** Card de un gasto: acento del deporte, icono, concepto, hijo o hijos, monto y estado de pago. */
@Composable
private fun ExpenseRow(expense: Expense, athletes: Map<String, ExpenseAthlete>) {
    val colors = MaterialTheme.colorScheme
    val pending = expense.status == PaymentStatus.PENDING
    val who = expense.athleteIds.mapNotNull { athletes[it] }
    SkCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        accentColor = expense.sport.colors().content,
    ) {
        Row(Modifier.padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(colors.surfaceContainer, MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                Icon(painterResource(expense.icon ?: expense.sport.iconRes), contentDescription = null, tint = expense.sport.colors().content)
            }
            Spacer(Modifier.width(SportKidTheme.spacing.lg))
            Column(Modifier.weight(1f)) {
                Text(expense.title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        when (who.size) {
                            1 -> who.first().firstName
                            else -> stringResource(R.string.expenses_and_names, who.dropLast(1).joinToString { it.firstName }, who.lastOrNull()?.firstName.orEmpty())
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (who.size == 1) who.first().color() else colors.onSurfaceVariant,
                    )
                    Text(stringResource(R.string.expenses_athlete_sport, stringResource(expense.sport.labelRes)), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal), color = colors.onSurfaceVariant)
                }
                Text(expense.concept, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(Modifier.padding(start = 4.dp), horizontalAlignment = Alignment.End) {
                Text(
                    stringResource(R.string.expenses_amount_mxn, expense.amountPesos.grouped()),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (pending) colors.tertiary else colors.onSurface,
                )
                val success = SportKidTheme.sportColors
                SkPill(
                    stringResource(if (pending) R.string.expenses_pending else R.string.expenses_paid),
                    modifier = Modifier.padding(top = 4.dp),
                    containerColor = if (pending) colors.tertiaryContainer else success.successContainer,
                    contentColor = if (pending) colors.onTertiaryContainer else success.success,
                    icon = painterResource(if (pending) R.drawable.ic_expenses_pending else UiR.drawable.ic_status_paid),
                    emphasized = true,
                    spacing = 2.dp,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                    textStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 15.sp),
                )
            }
        }
    }
}

@SkPreviews
@Composable
private fun ExpensesScreenPreview() = SportKidTheme {
    ExpensesScreen(today = LocalDate.of(2026, 11, 7))
}
