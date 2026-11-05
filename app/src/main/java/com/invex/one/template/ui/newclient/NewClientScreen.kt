package com.invex.one.template.ui.newclient

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.template.R
import com.invex.one.template.ui.components.VuelveDetailTopBar
import com.invex.one.template.ui.components.VuelveTextField
import com.invex.one.template.ui.components.subtleShadow
import com.invex.one.template.ui.theme.StatusOverdueDot
import com.invex.one.template.ui.theme.TemplateTheme
import com.invex.one.template.ui.theme.VuelveBackground
import com.invex.one.template.ui.theme.VuelveBlue600
import com.invex.one.template.ui.theme.VuelveOnPrimaryContainer
import com.invex.one.template.ui.theme.VuelveOnSurface
import com.invex.one.template.ui.theme.VuelveOnSurfaceVariant
import com.invex.one.template.ui.theme.VuelveOutline
import com.invex.one.template.ui.theme.VuelvePrimary
import com.invex.one.template.ui.theme.VuelvePrimaryContainer
import com.invex.one.template.ui.theme.VuelveSuccess
import com.invex.one.template.ui.theme.VuelveSurfaceContainer
import com.invex.one.template.ui.theme.VuelveSurfaceContainerLow

// region Model

enum class PetSpecies(@StringRes val label: Int, @DrawableRes val icon: Int) {
    Dog(R.string.newclient_species_dog, R.drawable.ic_newclient_dog),
    Cat(R.string.newclient_species_cat, R.drawable.ic_business_paw_service),
    Other(R.string.newclient_species_other, R.drawable.ic_newclient_more_horiz),
}

enum class UsualService(@StringRes val label: Int, @DrawableRes val icon: Int) {
    CutAndBath(R.string.newclient_service_cut, R.drawable.ic_register_scissors),
    BathOnly(R.string.newclient_service_bath, R.drawable.ic_newclient_svc_bath),
    HygienicCut(R.string.newclient_service_hygienic, R.drawable.ic_newclient_svc_hygienic),
    Deshedding(R.string.newclient_service_deshedding, R.drawable.ic_newclient_svc_deshedding),
    Treatment(R.string.newclient_service_treatment, R.drawable.ic_newclient_svc_treatment),
}

/** M14 — suggested visit frequency. [days] is null for "Personalizado". */
enum class VisitFrequency(@StringRes val label: Int, val days: Int?) {
    ThreeWeeks(R.string.newclient_frequency_3w, 21),
    FourWeeks(R.string.newclient_frequency_4w, 28),
    SixWeeks(R.string.newclient_frequency_6w, 42),
    Custom(R.string.newclient_frequency_custom, null),
}

enum class ContactChannel { WhatsApp, Call }

data class NewClientForm(
    val ownerName: String,
    val phone: String,
    val petName: String,
    val species: PetSpecies,
    val breed: String,
    val service: UsualService,
    val frequency: VisitFrequency,
    val notes: String,
    val channel: ContactChannel,
    val registerFirstVisit: Boolean,
) {
    val isValid: Boolean
        get() = ownerName.isNotBlank() && phone.isNotBlank() && petName.isNotBlank()
}

// endregion

/**
 * "Nuevo cliente recurrente" (Figma 96:3729). [onSave] receives the form and whether the
 * user chose to register the first visit right away (primary CTA or the checkbox row).
 */
@Composable
fun NewClientScreen(
    onBack: () -> Unit,
    onSave: (form: NewClientForm, registerVisit: Boolean) -> Unit,
) {
    var ownerName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var petName by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf(PetSpecies.Dog) }
    var breed by rememberSaveable { mutableStateOf("") }
    var service by rememberSaveable { mutableStateOf(UsualService.CutAndBath) }
    var frequency by rememberSaveable { mutableStateOf(VisitFrequency.FourWeeks) }
    var notes by rememberSaveable { mutableStateOf("") }
    var channel by rememberSaveable { mutableStateOf(ContactChannel.WhatsApp) }
    var registerFirstVisit by rememberSaveable { mutableStateOf(true) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    fun submit(registerVisit: Boolean) {
        val form = NewClientForm(
            ownerName, phone, petName, species, breed, service, frequency, notes, channel, registerFirstVisit,
        )
        if (form.isValid) onSave(form, registerVisit) else showErrors = true
    }

    Scaffold(
        containerColor = VuelveBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            VuelveDetailTopBar(
                title = stringResource(R.string.newclient_title),
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
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 40.dp),
        ) {
            WelcomeBanner(modifier = Modifier.padding(bottom = 16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionCard(
                    dot = VuelvePrimary,
                    title = stringResource(R.string.newclient_section_main),
                    trailing = {
                        Text(
                            text = stringResource(R.string.newclient_required_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = VuelveOutline,
                        )
                    },
                ) {
                    FormField(
                        label = stringResource(R.string.newclient_owner_label),
                        required = true,
                        error = showErrors && ownerName.isBlank(),
                    ) {
                        FilledField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            placeholder = stringResource(R.string.newclient_owner_placeholder),
                            icon = R.drawable.ic_register_person_outline,
                            capitalization = KeyboardCapitalization.Words,
                        )
                    }
                    FormField(
                        label = stringResource(R.string.newclient_phone_label),
                        required = true,
                        error = showErrors && phone.isBlank(),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .height(48.dp)
                                    .background(VuelveSurfaceContainerLow, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(text = stringResource(R.string.newclient_phone_flag), fontSize = 16.sp, lineHeight = 22.sp)
                                Text(
                                    text = stringResource(R.string.newclient_phone_prefix),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VuelveOnSurfaceVariant,
                                )
                            }
                            FilledField(
                                value = phone,
                                onValueChange = { phone = it },
                                placeholder = stringResource(R.string.newclient_phone_placeholder),
                                icon = R.drawable.ic_newclient_phone_field,
                                keyboardType = KeyboardType.Phone,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Image(painter = painterResource(R.drawable.ic_newclient_whatsapp_hint), contentDescription = null)
                            Text(
                                text = stringResource(R.string.newclient_phone_helper),
                                style = MaterialTheme.typography.bodySmall,
                                color = VuelveOnSurfaceVariant,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        FormField(
                            label = stringResource(R.string.newclient_pet_label),
                            required = true,
                            error = showErrors && petName.isBlank(),
                        ) {
                            FilledField(
                                value = petName,
                                onValueChange = { petName = it },
                                placeholder = stringResource(R.string.newclient_pet_placeholder),
                                icon = R.drawable.ic_newclient_pet_field,
                                capitalization = KeyboardCapitalization.Words,
                            )
                        }
                        FormField(label = stringResource(R.string.newclient_species_label)) {
                            Row(
                                modifier = Modifier.selectableGroup(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                PetSpecies.entries.forEach { option ->
                                    SpeciesOption(option = option, selected = option == species) { species = option }
                                }
                            }
                        }
                        FormField(label = stringResource(R.string.newclient_breed_label), optional = true) {
                            FilledField(
                                value = breed,
                                onValueChange = { breed = it },
                                placeholder = stringResource(R.string.newclient_breed_placeholder),
                                icon = R.drawable.ic_home_contacts,
                                capitalization = KeyboardCapitalization.Words,
                            )
                        }
                    }
                }

                SectionCard(dot = VuelveSuccess, title = stringResource(R.string.newclient_section_preferences)) {
                    ServiceChips(selected = service, onSelect = { service = it })
                    FrequencyPicker(
                        selected = frequency,
                        onSelect = { frequency = it },
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                SectionCard(dot = VuelveOutline, title = stringResource(R.string.newclient_section_details)) {
                    FormField(label = stringResource(R.string.newclient_notes_label), optional = true) {
                        NotesField(value = notes, onValueChange = { notes = it })
                    }
                    ChannelPicker(selected = channel, onSelect = { channel = it })
                }

                FirstVisitRow(checked = registerFirstVisit, onCheckedChange = { registerFirstVisit = it })

                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { submit(registerVisit = registerFirstVisit) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VuelveBlue600, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) {
                        Image(painter = painterResource(R.drawable.ic_newclient_save), contentDescription = null)
                        Text(
                            text = stringResource(R.string.newclient_save_and_visit),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VuelveSurfaceContainer)
                            .clickable(onClick = { submit(registerVisit = false) }, role = Role.Button),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.newclient_save_only),
                            style = MaterialTheme.typography.labelMedium,
                            color = VuelveOnSurface,
                        )
                    }
                }
            }
        }
    }
}

// region Building blocks

@Composable
private fun WelcomeBanner(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.05f))
            .clip(shape)
            .background(VuelveSurfaceContainerLow),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_newclient_mascot),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 16.dp, y = 16.dp),
        )
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(VuelvePrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(painter = painterResource(R.drawable.ic_home_paw_hero), contentDescription = null)
            }
            Column {
                Text(
                    text = stringResource(R.string.newclient_banner_title),
                    style = MaterialTheme.typography.titleLarge.copy(letterSpacing = (-0.18).sp),
                    color = VuelveOnSurface,
                )
                Text(
                    text = stringResource(R.string.newclient_banner_body),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 19.25.sp),
                    color = VuelveOnSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    dot: Color,
    title: String,
    trailing: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow(shape)
            .background(Color.White, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dot, CircleShape),
                )
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = VuelveOnSurface)
            }
            Box(modifier = Modifier.padding(start = 8.dp)) { trailing() }
        }
        content()
    }
}

/** M2 — label (+ required asterisk or "Opcional"), the field and an error line when needed. */
@Composable
private fun FormField(
    label: String,
    required: Boolean = false,
    optional: Boolean = false,
    error: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (optional) Arrangement.SpaceBetween else Arrangement.spacedBy(4.dp),
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = VuelveOnSurface)
            if (required) {
                Text(
                    text = stringResource(R.string.newclient_required_mark),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = StatusOverdueDot,
                )
            }
            if (optional) {
                Text(
                    text = stringResource(R.string.newclient_optional),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                    color = VuelveOutline,
                )
            }
        }
        content()
        if (error) {
            Text(
                text = stringResource(R.string.newclient_required_error),
                style = MaterialTheme.typography.bodySmall,
                color = StatusOverdueDot,
            )
        }
    }
}

/** Filled 48dp input: icon centered 24dp from the start, text from 44dp (as in Figma). */
@Composable
private fun FilledField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
) {
    VuelveTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        startPadding = 14.dp,
        endPadding = 16.dp,
        leadingSpacing = 10.dp,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = capitalization),
        leading = {
            Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(icon), contentDescription = null, tint = VuelveOutline)
            }
        },
    )
}

@Composable
private fun RowScope.SpeciesOption(option: PetSpecies, selected: Boolean, onClick: () -> Unit) {
    val contentColor = if (selected) Color.White else VuelveOnSurfaceVariant
    Row(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .then(if (selected) Modifier.subtleShadow(RoundedCornerShape(8.dp)) else Modifier)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) VuelvePrimary else VuelveSurfaceContainerLow)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        Icon(painter = painterResource(option.icon), contentDescription = null, tint = contentColor)
        Text(text = stringResource(option.label), style = MaterialTheme.typography.labelMedium, color = contentColor)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceChips(selected: UsualService, onSelect: (UsualService) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.newclient_service_label),
            style = MaterialTheme.typography.labelMedium,
            color = VuelveOnSurface,
        )
        FlowRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UsualService.entries.forEach { option ->
                val isSelected = option == selected
                val contentColor = if (isSelected) Color.White else VuelveOnSurfaceVariant
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) VuelvePrimary else VuelveSurfaceContainerLow)
                        .selectable(selected = isSelected, onClick = { onSelect(option) }, role = Role.RadioButton)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(painter = painterResource(option.icon), contentDescription = null, tint = contentColor)
                    Text(
                        text = stringResource(option.label),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequencyPicker(
    selected: VisitFrequency,
    onSelect: (VisitFrequency) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_newclient_frequency_label), contentDescription = null)
                Text(
                    text = stringResource(R.string.newclient_frequency_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = VuelveOnSurface,
                )
            }
            Text(
                text = stringResource(R.string.newclient_frequency_hint),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = VuelvePrimary,
            )
        }
        // 2x2 grid (README M14).
        Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VisitFrequency.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { option ->
                        FrequencyTile(option = option, selected = option == selected) { onSelect(option) }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .background(VuelveBlue600.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(painter = painterResource(R.drawable.ic_newclient_robot_margin), contentDescription = null)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.newclient_callout_brand))
                    }
                    append(stringResource(R.string.newclient_callout_body))
                },
                style = MaterialTheme.typography.bodySmall,
                color = VuelveOnSurface,
            )
        }
    }
}

@Composable
private fun RowScope.FrequencyTile(option: VisitFrequency, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .then(if (selected) Modifier.subtleShadow(RoundedCornerShape(8.dp)) else Modifier)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) VuelvePrimaryContainer else VuelveSurfaceContainerLow)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(option.label),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            ),
            color = if (selected) VuelveOnPrimaryContainer else VuelveOnSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false),
        )
        when {
            selected -> Image(
                painter = painterResource(R.drawable.ic_newclient_check_circle),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp),
            )
            option == VisitFrequency.Custom -> Image(
                painter = painterResource(R.drawable.ic_newclient_tune),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun NotesField(value: String, onValueChange: (String) -> Unit) {
    val style = MaterialTheme.typography.bodyMedium.copy(color = VuelveOnSurface)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = style,
        cursorBrush = SolidColor(VuelvePrimary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .background(VuelveSurfaceContainerLow, RoundedCornerShape(8.dp))
                    .padding(12.dp),
            ) {
                if (value.isEmpty()) {
                    Text(text = stringResource(R.string.newclient_notes_placeholder), style = style, color = VuelveOutline)
                }
                inner()
            }
        },
    )
}

@Composable
private fun ChannelPicker(selected: ContactChannel, onSelect: (ContactChannel) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.newclient_channel_label),
            style = MaterialTheme.typography.labelMedium,
            color = VuelveOnSurface,
        )
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChannelOption(
                icon = R.drawable.ic_followups_chat,
                label = stringResource(R.string.newclient_channel_whatsapp),
                selected = selected == ContactChannel.WhatsApp,
                onClick = { onSelect(ContactChannel.WhatsApp) },
            )
            ChannelOption(
                icon = R.drawable.ic_newclient_call,
                label = stringResource(R.string.newclient_channel_call),
                selected = selected == ContactChannel.Call,
                onClick = { onSelect(ContactChannel.Call) },
            )
        }
    }
}

/** M15 — selected channel is solid deep green; the alternative is tonal. */
@Composable
private fun RowScope.ChannelOption(@DrawableRes icon: Int, label: String, selected: Boolean, onClick: () -> Unit) {
    val contentColor = if (selected) Color.White else VuelveOnSurfaceVariant
    Row(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .then(if (selected) Modifier.subtleShadow(RoundedCornerShape(8.dp)) else Modifier)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) VuelveSuccess else VuelveSurfaceContainerLow)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = contentColor)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

/** M16 — checkbox option row. */
@Composable
private fun FirstVisitRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VuelveSurfaceContainerLow.copy(alpha = 0.7f))
            .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Checkbox)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .then(
                        if (checked) {
                            Modifier.background(VuelvePrimary)
                        } else {
                            Modifier
                                .background(Color.White)
                                .border(1.5.dp, VuelveOutline, RoundedCornerShape(6.dp))
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (checked) {
                    Image(painter = painterResource(R.drawable.ic_newclient_check), contentDescription = null)
                }
            }
            Column {
                Text(
                    text = stringResource(R.string.newclient_first_visit_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = VuelveOnSurface,
                )
                Text(
                    text = stringResource(R.string.newclient_first_visit_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = VuelveOnSurfaceVariant,
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.ic_newclient_chevron_right),
            contentDescription = null,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

// endregion

@Preview(showBackground = true, widthDp = 390, heightDp = 1900)
@Composable
private fun NewClientScreenPreview() {
    TemplateTheme {
        NewClientScreen(onBack = {}, onSave = { _, _ -> })
    }
}
