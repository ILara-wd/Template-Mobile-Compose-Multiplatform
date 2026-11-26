package com.invex.one.template.feature.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.atom.CategoryTag
import com.invex.one.template.core.designsystem.atom.SmallTonalIconButton
import com.invex.one.template.core.designsystem.atom.TagSize
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.AppDatePickerDialog
import com.invex.one.template.core.designsystem.molecule.AppPickerField
import com.invex.one.template.core.designsystem.molecule.AppTextField
import com.invex.one.template.core.designsystem.molecule.FieldLabel
import com.invex.one.template.core.designsystem.molecule.FieldSupport
import com.invex.one.template.core.designsystem.molecule.formatUtcDate
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppAlpha
import com.invex.one.template.core.designsystem.token.AppSize
import com.invex.one.template.feature.profile.R
import com.invex.one.template.feature.profile.model.AgeBracket
import com.invex.one.template.feature.profile.model.ProfileForm
import com.invex.one.template.feature.profile.model.ProfileUiState
import com.invex.one.template.feature.profile.model.ServerRegion
import com.invex.one.template.feature.profile.model.ageBracket
import com.invex.one.template.feature.profile.model.ageInYears

/** Events of the personal data form. */
internal data class PersonalDataCallbacks(
    val onGamertagChange: (String) -> Unit,
    val onFullNameChange: (String) -> Unit,
    val onEmailChange: (String) -> Unit,
    val onUpdateEmail: () -> Unit,
    val onBirthdateSelected: (Long?) -> Unit,
    val onRegionSelected: (ServerRegion) -> Unit,
)

/** "Datos de Jugador" card: gamertag, name, email, birthdate (with age) and server region. */
@Composable
internal fun PersonalDataCard(
    form: ProfileForm,
    gamertagAvailable: Boolean,
    editable: Boolean,
    callbacks: PersonalDataCallbacks,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val shape = AppTheme.shapes.large
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showRegions by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .appShadow(AppTheme.elevation.md, shape, colors.shadow)
            .background(colors.surfaceContainerLowest, shape)
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.xxs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.profile_data_title), style = AppTheme.typography.title, color = colors.onSurface)
            CategoryTag(
                stringResource(R.string.profile_status_active),
                colors.primaryFixed,
                colors.onPrimaryFixed,
                size = TagSize.Small,
                uppercase = false,
            )
        }
        AppTextField(
            value = form.gamertag,
            onValueChange = callbacks.onGamertagChange,
            label = FieldLabel(stringResource(R.string.profile_gamertag_label), AppIcons.GamepadField),
            placeholder = "",
            leadingIcon = null,
            labelAction = if (gamertagAvailable) ({ StatusChip(stringResource(R.string.profile_gamertag_available)) }) else null,
            trailing = {
                Text(
                    stringResource(R.string.profile_counter, form.gamertag.length, ProfileUiState.MAX_GAMERTAG),
                    style = AppTheme.typography.label,
                    color = colors.onSurfaceVariant,
                )
            },
            support = FieldSupport(stringResource(R.string.profile_gamertag_support)),
            height = AppSize.fieldHeightCompact,
            textStyle = AppTheme.typography.bodyLarge,
            enabled = editable,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        AppTextField(
            value = form.fullName,
            onValueChange = callbacks.onFullNameChange,
            label = FieldLabel(stringResource(R.string.profile_name_label), AppIcons.PersonOutline),
            placeholder = stringResource(R.string.profile_name_placeholder),
            leadingIcon = null,
            height = AppSize.fieldHeightCompact,
            textStyle = AppTheme.typography.bodyLarge,
            enabled = editable,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        AppTextField(
            value = form.email,
            onValueChange = callbacks.onEmailChange,
            label = FieldLabel(stringResource(R.string.profile_email_label), AppIcons.MailOutline),
            placeholder = stringResource(R.string.profile_email_placeholder),
            leadingIcon = null,
            labelAction = if (form.emailVerified) ({ VerifiedLabel() }) else null,
            trailing = {
                SmallTonalIconButton(AppIcons.Edit, stringResource(R.string.profile_update_email), callbacks.onUpdateEmail, enabled = editable)
            },
            height = AppSize.fieldHeightCompact,
            enabled = editable,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
        )
        AppPickerField(
            value = form.birthdateMillis?.let { formatUtcDate(it, "dd / MM / yyyy") },
            placeholder = stringResource(R.string.profile_birthdate_placeholder),
            onClick = { showDatePicker = true },
            label = FieldLabel(stringResource(R.string.profile_birthdate_label), AppIcons.CakeOutline),
            leadingIcon = null,
            labelAction = form.birthdateMillis?.let { millis -> { AgeChip(ageInYears(millis)) } },
            trailingIcon = AppIcons.CalendarOutline,
            support = FieldSupport(stringResource(R.string.profile_birthdate_support)),
            height = AppSize.fieldHeightCompact,
            enabled = editable,
        )
        Box {
            AppPickerField(
                value = form.region.display(),
                placeholder = "",
                onClick = { showRegions = true },
                label = FieldLabel(stringResource(R.string.profile_region_label), AppIcons.GlobeOutline),
                leadingIcon = null,
                trailingIcon = AppIcons.ChevronDown,
                height = AppSize.fieldHeightCompact,
                enabled = editable,
            )
            DropdownMenu(expanded = showRegions, onDismissRequest = { showRegions = false }) {
                ServerRegion.entries.forEach { region ->
                    DropdownMenuItem(
                        text = { Text(region.display(), style = AppTheme.typography.body, color = colors.onSurface) },
                        onClick = {
                            callbacks.onRegionSelected(region)
                            showRegions = false
                        },
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialMillis = form.birthdateMillis,
            confirmLabel = stringResource(R.string.profile_date_confirm),
            dismissLabel = stringResource(R.string.profile_date_cancel),
            onConfirm = { callbacks.onBirthdateSelected(it); showDatePicker = false },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun ServerRegion.display(): String =
    stringResource(R.string.profile_region_format, flag, stringResource(country), stringResource(server))

/** Tinted primary pill with a check icon (e.g. gamertag "Disponible"). */
@Composable
private fun StatusChip(text: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .background(colors.primary.copy(alpha = AppAlpha.Ambient), AppTheme.shapes.pill)
            .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xxxs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(AppIcons.CheckMini)
        Text(text, style = AppTheme.typography.label, color = colors.primary)
    }
}

/** Verified badge shown next to a confirmed email. */
@Composable
private fun VerifiedLabel() {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxxs), verticalAlignment = Alignment.CenterVertically) {
        AppIcon(AppIcons.VerifiedSmall)
        Text(stringResource(R.string.profile_email_verified), style = AppTheme.typography.label, color = AppTheme.colors.primary)
    }
}

/** Chip with the computed age and its category bracket (Kids / Junior / Senior). */
@Composable
private fun AgeChip(age: Int) {
    val bracket = stringResource(
        when (ageBracket(age)) {
            AgeBracket.Senior -> R.string.profile_age_senior
            AgeBracket.Junior -> R.string.profile_age_junior
            AgeBracket.Kids -> R.string.profile_age_kids
        },
    )
    Text(
        pluralStringResource(R.plurals.profile_age, age, age, bracket),
        style = AppTheme.typography.label,
        color = AppTheme.colors.onSurfaceVariant,
        modifier = Modifier
            .background(AppTheme.colors.surfaceContainer, AppTheme.shapes.pill)
            .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xxxs),
    )
}
