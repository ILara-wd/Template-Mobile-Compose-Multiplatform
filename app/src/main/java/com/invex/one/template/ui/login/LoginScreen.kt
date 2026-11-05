package com.invex.one.template.ui.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.template.R
import com.invex.one.template.ui.components.VuelveButton
import com.invex.one.template.ui.components.VuelveFlowScaffold
import com.invex.one.template.ui.components.VuelveTextField
import com.invex.one.template.ui.components.subtleShadow
import com.invex.one.template.ui.theme.TemplateTheme
import com.invex.one.template.ui.theme.VuelveMint
import com.invex.one.template.ui.theme.VuelveOnPrimaryContainer
import com.invex.one.template.ui.theme.VuelveOnSurface
import com.invex.one.template.ui.theme.VuelveOnSurfaceVariant
import com.invex.one.template.ui.theme.VuelveOutline
import com.invex.one.template.ui.theme.VuelveOutlineVariant
import com.invex.one.template.ui.theme.VuelvePrimary
import com.invex.one.template.ui.theme.VuelvePrimaryContainer
import com.invex.one.template.ui.theme.VuelveSuccess
import com.invex.one.template.ui.theme.VuelveSurfaceContainer
import com.invex.one.template.ui.theme.VuelveSurfaceContainerLow

enum class LoginMethod { Phone, Email }

/** Stateful entry point for the "Iniciar sesión — Vuelve" screen (Figma 87:2049). */
@Composable
fun LoginScreen(
    onBack: () -> Unit = {},
    onProfile: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onSubmit: (method: LoginMethod, user: String, password: String) -> Unit = { _, _, _ -> },
    onWhatsAppCode: () -> Unit = {},
    onGoogle: () -> Unit = {},
    onRegister: () -> Unit = {},
) {
    var method by rememberSaveable { mutableStateOf(LoginMethod.Phone) }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LoginContent(
        method = method,
        onMethodChange = { method = it },
        phone = phone,
        onPhoneChange = { phone = it },
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        passwordVisible = passwordVisible,
        onTogglePasswordVisible = { passwordVisible = !passwordVisible },
        onBack = onBack,
        onProfile = onProfile,
        onForgotPassword = onForgotPassword,
        onSubmit = {
            onSubmit(method, if (method == LoginMethod.Phone) phone else email, password)
        },
        onWhatsAppCode = onWhatsAppCode,
        onGoogle = onGoogle,
        onRegister = onRegister,
    )
}

@Composable
fun LoginContent(
    method: LoginMethod,
    onMethodChange: (LoginMethod) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onForgotPassword: () -> Unit,
    onSubmit: () -> Unit,
    onWhatsAppCode: () -> Unit,
    onGoogle: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VuelveFlowScaffold(
        title = stringResource(R.string.login_title),
        onBack = onBack,
        onProfile = onProfile,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            LoginHero()
            SocialProofCard()
            LoginFormCard(
                method = method,
                onMethodChange = onMethodChange,
                phone = phone,
                onPhoneChange = onPhoneChange,
                email = email,
                onEmailChange = onEmailChange,
                password = password,
                onPasswordChange = onPasswordChange,
                passwordVisible = passwordVisible,
                onTogglePasswordVisible = onTogglePasswordVisible,
                onForgotPassword = onForgotPassword,
                onSubmit = onSubmit,
                onWhatsAppCode = onWhatsAppCode,
                onGoogle = onGoogle,
            )
            LoginFooter(onRegister = onRegister)
        }
    }
}

// region Hero

@Composable
private fun LoginHero() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(4.dp))
        Box(modifier = Modifier.size(64.dp)) {
            val tileShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .subtleShadow(tileShape)
                    .background(VuelveSurfaceContainer, tileShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(painter = painterResource(R.drawable.ic_login_paw), contentDescription = null)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(20.dp)
                    .background(VuelveMint, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(painter = painterResource(R.drawable.ic_vuelve_check_small), contentDescription = null)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.login_brand),
                style = MaterialTheme.typography.displaySmall,
                color = VuelvePrimary,
            )
            Text(
                text = stringResource(R.string.login_brand_pro),
                style = TextStyle(
                    fontSize = 10.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                ),
                color = VuelveOnPrimaryContainer,
                modifier = Modifier
                    .background(VuelvePrimaryContainer, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.login_welcome),
            style = MaterialTheme.typography.titleMedium,
            color = VuelveOnSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = VuelveOnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
    }
}

// endregion

// region Social proof

@Composable
private fun SocialProofCard() {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow(shape)
            .background(VuelveSurfaceContainerLow, shape)
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
                    .size(36.dp)
                    .background(VuelveMint, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(painter = painterResource(R.drawable.ic_login_bell), contentDescription = null)
            }
            Column {
                Text(
                    text = stringResource(R.string.login_social_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = VuelveOnSurface,
                )
                Text(
                    text = stringResource(R.string.login_social_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = VuelveOnSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.login_social_metric),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = VuelveSuccess,
            modifier = Modifier
                .background(Color.White, CircleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

// endregion

// region Form

@Composable
private fun LoginFormCard(
    method: LoginMethod,
    onMethodChange: (LoginMethod) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    onForgotPassword: () -> Unit,
    onSubmit: () -> Unit,
    onWhatsAppCode: () -> Unit,
    onGoogle: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .subtleShadow(shape)
            .background(Color.White, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LoginMethodSwitcher(selected = method, onSelect = onMethodChange)

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (method == LoginMethod.Phone) {
                LabeledField(label = { FieldLabel(stringResource(R.string.login_phone_label)) }) {
                    VuelveTextField(
                        value = phone,
                        onValueChange = onPhoneChange,
                        placeholder = stringResource(R.string.login_phone_placeholder),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leading = {
                            Row(
                                modifier = Modifier.padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.login_phone_flag),
                                    fontSize = 18.sp,
                                    lineHeight = 20.sp,
                                )
                                Text(
                                    text = stringResource(R.string.login_phone_prefix),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VuelveOnSurface,
                                )
                            }
                        },
                    )
                }
            } else {
                LabeledField(label = { FieldLabel(stringResource(R.string.login_email_label)) }) {
                    VuelveTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        placeholder = stringResource(R.string.login_email_placeholder),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leading = {
                            Icon(
                                painter = painterResource(R.drawable.ic_login_mail),
                                contentDescription = null,
                                tint = VuelveOutline,
                            )
                        },
                    )
                }
            }

            LabeledField(
                label = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        FieldLabel(stringResource(R.string.login_password_label))
                        Text(
                            text = stringResource(R.string.login_forgot_password),
                            style = MaterialTheme.typography.labelSmall,
                            color = VuelvePrimary,
                            modifier = Modifier.clickable(onClick = onForgotPassword, role = Role.Button),
                        )
                    }
                },
            ) {
                VuelveTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    placeholder = stringResource(R.string.login_password_placeholder),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    leading = {
                        Icon(painter = painterResource(R.drawable.ic_vuelve_lock), contentDescription = null, tint = VuelveOutline)
                    },
                    trailing = {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(25.5.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onTogglePasswordVisible, role = Role.Button),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_vuelve_eye),
                                contentDescription = stringResource(
                                    if (passwordVisible) R.string.login_hide_password else R.string.login_show_password,
                                ),
                                tint = VuelveOutline,
                            )
                        }
                    },
                )
            }

            VuelveButton(
                onClick = onSubmit,
                containerColor = VuelvePrimary,
                contentColor = Color.White,
                horizontalSpacing = 8.dp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .subtleShadow(RoundedCornerShape(12.dp)),
            ) {
                Text(text = stringResource(R.string.login_submit), style = MaterialTheme.typography.labelLarge)
                Image(painter = painterResource(R.drawable.ic_login_arrow_forward), contentDescription = null)
            }
        }

        TextDivider(text = stringResource(R.string.login_divider))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            VuelveButton(
                onClick = onWhatsAppCode,
                containerColor = VuelveSuccess,
                contentColor = Color.White,
            ) {
                Image(painter = painterResource(R.drawable.ic_login_chat), contentDescription = null)
                Text(text = stringResource(R.string.login_whatsapp_code), style = MaterialTheme.typography.labelLarge)
            }
            VuelveButton(
                onClick = onGoogle,
                containerColor = VuelveSurfaceContainerLow,
                contentColor = VuelveOnSurface,
            ) {
                Image(painter = painterResource(R.drawable.ic_login_google), contentDescription = null)
                Text(text = stringResource(R.string.login_google), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun LoginMethodSwitcher(selected: LoginMethod, onSelect: (LoginMethod) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VuelveSurfaceContainer, RoundedCornerShape(8.dp))
            .padding(4.dp),
    ) {
        SwitcherOption(
            selected = selected == LoginMethod.Phone,
            onClick = { onSelect(LoginMethod.Phone) },
            icon = R.drawable.ic_login_phone,
            label = stringResource(R.string.login_tab_phone),
        )
        SwitcherOption(
            selected = selected == LoginMethod.Email,
            onClick = { onSelect(LoginMethod.Email) },
            icon = R.drawable.ic_login_mail,
            label = stringResource(R.string.login_tab_email),
        )
    }
}

@Composable
private fun RowScope.SwitcherOption(
    selected: Boolean,
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    label: String,
) {
    val shape = RoundedCornerShape(6.dp)
    val contentColor = if (selected) VuelvePrimary else VuelveOnSurfaceVariant
    Row(
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick, role = Role.Tab)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = contentColor)
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = contentColor)
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelMedium, color = VuelveOnSurfaceVariant)
}

@Composable
private fun LabeledField(label: @Composable () -> Unit, field: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        label()
        field()
    }
}

@Composable
private fun TextDivider(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = VuelveOutlineVariant)
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = VuelveOutline,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = VuelveOutlineVariant)
    }
}

// endregion

// region Footer

@Composable
private fun LoginFooter(onRegister: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.login_no_account),
                style = MaterialTheme.typography.bodyMedium,
                color = VuelveOnSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.login_register),
                style = MaterialTheme.typography.labelLarge,
                color = VuelvePrimary,
                modifier = Modifier.clickable(onClick = onRegister, role = Role.Button),
            )
        }
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Image(painter = painterResource(R.drawable.ic_login_lock_small), contentDescription = null)
            Text(
                text = stringResource(R.string.login_data_protected),
                style = MaterialTheme.typography.labelSmall,
                color = VuelveOnSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// endregion

@Preview(showBackground = true, widthDp = 390, heightDp = 920)
@Composable
private fun LoginScreenPreview() {
    TemplateTheme {
        LoginScreen()
    }
}
