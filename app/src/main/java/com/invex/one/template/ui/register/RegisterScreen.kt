package com.invex.one.template.ui.register

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.template.R
import com.invex.one.template.ui.components.VuelveButton
import com.invex.one.template.ui.components.VuelveFlowScaffold
import com.invex.one.template.ui.components.VuelveTextField
import com.invex.one.template.ui.components.subtleShadow
import com.invex.one.template.ui.theme.TemplateTheme
import com.invex.one.template.ui.theme.VuelveBlue600
import com.invex.one.template.ui.theme.VuelveMint
import com.invex.one.template.ui.theme.VuelveMintSoft
import com.invex.one.template.ui.theme.VuelveOnMint
import com.invex.one.template.ui.theme.VuelveOnSurface
import com.invex.one.template.ui.theme.VuelveOnSurfaceVariant
import com.invex.one.template.ui.theme.VuelveOutline
import com.invex.one.template.ui.theme.VuelvePrimary
import com.invex.one.template.ui.theme.VuelveSecondaryContainer
import com.invex.one.template.ui.theme.VuelveSuccess
import com.invex.one.template.ui.theme.VuelveSurfaceContainer
import com.invex.one.template.ui.theme.VuelveSurfaceContainerHigh
import com.invex.one.template.ui.theme.VuelveSurfaceContainerLow

enum class BusinessService(@StringRes val label: Int, @DrawableRes val icon: Int) {
    Grooming(R.string.register_service_grooming, R.drawable.ic_register_scissors),
    VetAndBath(R.string.register_service_vet, R.drawable.ic_register_medical),
    MobileGrooming(R.string.register_service_mobile, R.drawable.ic_register_car),
    Other(R.string.register_service_other, R.drawable.ic_register_tune),
}

data class RegisterForm(
    val businessName: String,
    val ownerName: String,
    val phone: String,
    val services: Set<BusinessService>,
    val password: String,
    val acceptedTerms: Boolean,
)

/** Stateful entry point for "Crear cuenta — Registro de negocio" (Figma 89:2171). */
@Composable
fun RegisterScreen(
    onBack: () -> Unit = {},
    onProfile: () -> Unit = {},
    onSubmit: (RegisterForm) -> Unit = {},
    onSignIn: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
) {
    var businessName by rememberSaveable { mutableStateOf("") }
    var ownerName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var services by rememberSaveable { mutableStateOf(setOf(BusinessService.Grooming)) }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var acceptedTerms by rememberSaveable { mutableStateOf(false) }

    RegisterContent(
        businessName = businessName,
        onBusinessNameChange = { businessName = it },
        ownerName = ownerName,
        onOwnerNameChange = { ownerName = it },
        phone = phone,
        onPhoneChange = { phone = it },
        services = services,
        onToggleService = { services = if (it in services) services - it else services + it },
        password = password,
        onPasswordChange = { password = it },
        passwordVisible = passwordVisible,
        onTogglePasswordVisible = { passwordVisible = !passwordVisible },
        acceptedTerms = acceptedTerms,
        onAcceptedTermsChange = { acceptedTerms = it },
        onBack = onBack,
        onProfile = onProfile,
        onSubmit = {
            onSubmit(RegisterForm(businessName, ownerName, phone, services, password, acceptedTerms))
        },
        onSignIn = onSignIn,
        onOpenTerms = onOpenTerms,
        onOpenPrivacy = onOpenPrivacy,
    )
}

@Composable
fun RegisterContent(
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
    ownerName: String,
    onOwnerNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    services: Set<BusinessService>,
    onToggleService: (BusinessService) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    acceptedTerms: Boolean,
    onAcceptedTermsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onSubmit: () -> Unit,
    onSignIn: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VuelveFlowScaffold(
        title = stringResource(R.string.register_title),
        onBack = onBack,
        onProfile = onProfile,
        modifier = modifier,
    ) {
        RegisterIntro()
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FormGroup(
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            FieldLabel(stringResource(R.string.register_business_label))
                            Text(
                                text = stringResource(R.string.register_required),
                                style = MaterialTheme.typography.labelSmall,
                                color = VuelvePrimary,
                            )
                        }
                    },
                ) {
                    RegisterTextField(
                        value = businessName,
                        onValueChange = onBusinessNameChange,
                        placeholder = stringResource(R.string.register_business_placeholder),
                        icon = R.drawable.ic_register_store,
                    )
                }

                FormGroup(label = { FieldLabel(stringResource(R.string.register_owner_label)) }) {
                    RegisterTextField(
                        value = ownerName,
                        onValueChange = onOwnerNameChange,
                        placeholder = stringResource(R.string.register_owner_placeholder),
                        icon = R.drawable.ic_register_person_outline,
                    )
                }

                PhoneGroup(phone = phone, onPhoneChange = onPhoneChange)

                ServicesGroup(selected = services, onToggle = onToggleService)

                FormGroup(label = { FieldLabel(stringResource(R.string.register_password_label)) }) {
                    RegisterTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        placeholder = stringResource(R.string.register_password_placeholder),
                        icon = R.drawable.ic_vuelve_lock,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailing = {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onTogglePasswordVisible, role = Role.Button),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_vuelve_eye),
                                    contentDescription = stringResource(R.string.register_password_toggle),
                                    tint = VuelveOnSurfaceVariant,
                                )
                            }
                        },
                    )
                }

                TermsRow(
                    checked = acceptedTerms,
                    onCheckedChange = onAcceptedTermsChange,
                    onOpenTerms = onOpenTerms,
                    onOpenPrivacy = onOpenPrivacy,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    VuelveButton(
                        onClick = onSubmit,
                        containerColor = VuelveBlue600,
                        contentColor = Color.White,
                        horizontalSpacing = 8.dp,
                        modifier = Modifier.shadow(4.dp, RoundedCornerShape(12.dp)),
                    ) {
                        Text(
                            text = stringResource(R.string.register_submit),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_vuelve_arrow_forward_md),
                            contentDescription = null,
                        )
                    }
                    SignInLink(onSignIn = onSignIn)
                }
            }

            TrustBanner(modifier = Modifier.padding(top = 4.dp))
            MessagePreviewCard()
        }
    }
}

// region Intro

@Composable
private fun RegisterIntro() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val badgeShape = CircleShape
        Row(
            modifier = Modifier
                .subtleShadow(badgeShape)
                .background(VuelveMint, badgeShape)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Image(painter = painterResource(R.drawable.ic_vuelve_paw_small), contentDescription = null)
            Text(
                text = stringResource(R.string.register_brand_badge),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
                color = VuelveOnMint,
            )
        }
        Text(
            text = stringResource(R.string.register_heading),
            style = MaterialTheme.typography.headlineSmall,
            color = VuelveOnSurface,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = stringResource(R.string.register_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = VuelveOnSurfaceVariant,
        )
    }
}

// endregion

// region Form fields

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelMedium, color = VuelveOnSurface)
}

@Composable
private fun FormGroup(label: @Composable () -> Unit, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        label()
        content()
    }
}

/** Register-style field: 12dp corners, shadow, icon centered 24dp from the start, text at 44dp. */
@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    @DrawableRes icon: Int,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    VuelveTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        shape = RoundedCornerShape(12.dp),
        withShadow = true,
        startPadding = 14.dp,
        endPadding = if (trailing != null) 8.dp else 16.dp,
        leadingSpacing = 10.dp,
        trailingSpacing = 0.dp,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        leading = {
            Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(icon), contentDescription = null, tint = VuelveOnSurfaceVariant)
            }
        },
        trailing = trailing,
    )
}

@Composable
private fun PhoneGroup(phone: String, onPhoneChange: (String) -> Unit) {
    FormGroup(
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FieldLabel(stringResource(R.string.register_phone_label))
                Image(painter = painterResource(R.drawable.ic_register_whatsapp_label), contentDescription = null)
            }
        },
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val chipShape = RoundedCornerShape(12.dp)
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .subtleShadow(chipShape)
                        .background(VuelveSurfaceContainerHigh, chipShape)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(text = stringResource(R.string.register_phone_flag), fontSize = 16.sp, lineHeight = 24.sp)
                    Text(
                        text = stringResource(R.string.register_phone_prefix),
                        style = MaterialTheme.typography.labelLarge,
                        color = VuelveOnSurface,
                    )
                }
                VuelveTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    placeholder = stringResource(R.string.register_phone_placeholder),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    withShadow = true,
                    startPadding = 14.dp,
                    endPadding = 14.dp,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.35.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_register_info), contentDescription = null)
                Text(
                    text = stringResource(R.string.register_phone_helper),
                    style = MaterialTheme.typography.bodySmall,
                    color = VuelveOnSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServicesGroup(selected: Set<BusinessService>, onToggle: (BusinessService) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel(stringResource(R.string.register_services_label))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BusinessService.entries.forEach { service ->
                ServiceChip(
                    service = service,
                    selected = service in selected,
                    onClick = { onToggle(service) },
                )
            }
        }
    }
}

@Composable
private fun ServiceChip(service: BusinessService, selected: Boolean, onClick: () -> Unit) {
    val shape = CircleShape
    val contentColor = if (selected) Color.White else VuelveOnSurface
    Row(
        modifier = Modifier
            .subtleShadow(shape)
            .clip(shape)
            .background(if (selected) VuelveBlue600 else VuelveSurfaceContainerHigh)
            .toggleable(value = selected, onValueChange = { onClick() }, role = Role.Checkbox)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(painter = painterResource(service.icon), contentDescription = null, tint = contentColor)
        Text(
            text = stringResource(service.label),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun TermsRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val linkStyles = TextLinkStyles(SpanStyle(color = VuelvePrimary, textDecoration = TextDecoration.Underline))
    val text = buildAnnotatedString {
        append(stringResource(R.string.register_terms_prefix))
        withLink(LinkAnnotation.Clickable("terms", linkStyles) { onOpenTerms() }) {
            append(stringResource(R.string.register_terms_link))
        }
        append(stringResource(R.string.register_terms_middle))
        withLink(LinkAnnotation.Clickable("privacy", linkStyles) { onOpenPrivacy() }) {
            append(stringResource(R.string.register_privacy_link))
        }
        append(stringResource(R.string.register_terms_suffix))
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        VuelveCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = VuelveOnSurfaceVariant)
    }
}

/** 16dp square checkbox with 2.5dp corners (filled primary when checked). */
@Composable
private fun VuelveCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(2.5.dp)
    Box(
        modifier = modifier
            .size(16.dp)
            .clip(shape)
            .then(
                if (checked) {
                    Modifier.background(VuelvePrimary)
                } else {
                    Modifier
                        .background(Color.White)
                        .border(1.5.dp, VuelveOutline, shape)
                },
            )
            .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Checkbox),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                painter = painterResource(R.drawable.ic_vuelve_check_small),
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun SignInLink(onSignIn: () -> Unit) {
    val text = buildAnnotatedString {
        append(stringResource(R.string.register_have_account))
        withLink(
            LinkAnnotation.Clickable(
                tag = "sign_in",
                styles = TextLinkStyles(SpanStyle(color = VuelvePrimary, fontWeight = FontWeight.Normal)),
            ) { onSignIn() },
        ) {
            append(stringResource(R.string.register_sign_in))
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = VuelveOnSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    )
}

// endregion

// region Trust + preview

@Composable
private fun TrustBanner(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .subtleShadow(shape)
            .background(VuelveSurfaceContainer, shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(VuelveMint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(painter = painterResource(R.drawable.ic_vuelve_verified), contentDescription = null)
        }
        Column {
            Text(
                text = stringResource(R.string.register_trust_title),
                style = MaterialTheme.typography.labelMedium,
                color = VuelveOnSurface,
            )
            Text(
                text = stringResource(R.string.register_trust_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = VuelveOnSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessagePreviewCard() {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow(shape)
            .background(VuelveSurfaceContainerLow, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(VuelveMintSoft, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painter = painterResource(R.drawable.ic_register_sparkle), contentDescription = null)
                }
                Text(
                    text = stringResource(R.string.register_preview_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = VuelveOnSurface,
                )
            }
            Text(
                text = stringResource(R.string.register_preview_badge),
                style = MaterialTheme.typography.labelSmall,
                color = VuelvePrimary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(VuelveSecondaryContainer, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        val bubbleShape = RoundedCornerShape(12.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .subtleShadow(bubbleShape)
                .background(Color.White, bubbleShape)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(painter = painterResource(R.drawable.ic_vuelve_check_circle_small), contentDescription = null)
                Text(
                    text = stringResource(R.string.register_preview_channel),
                    style = MaterialTheme.typography.labelSmall,
                    color = VuelveSuccess,
                )
            }
            Text(
                text = stringResource(R.string.register_preview_message),
                style = MaterialTheme.typography.bodySmall,
                color = VuelveOnSurface,
            )
        }
    }
}

// endregion

@Preview(showBackground = true, widthDp = 390, heightDp = 1020)
@Composable
private fun RegisterScreenPreview() {
    TemplateTheme {
        RegisterContent(
            businessName = "",
            onBusinessNameChange = {},
            ownerName = "",
            onOwnerNameChange = {},
            phone = "",
            onPhoneChange = {},
            services = setOf(BusinessService.Grooming),
            onToggleService = {},
            password = "",
            onPasswordChange = {},
            passwordVisible = false,
            onTogglePasswordVisible = {},
            acceptedTerms = true,
            onAcceptedTermsChange = {},
            onBack = {},
            onProfile = {},
            onSubmit = {},
            onSignIn = {},
            onOpenTerms = {},
            onOpenPrivacy = {},
        )
    }
}
