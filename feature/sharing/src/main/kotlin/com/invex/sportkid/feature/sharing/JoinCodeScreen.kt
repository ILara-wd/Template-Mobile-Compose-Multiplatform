package com.invex.sportkid.feature.sharing

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkHelpButton
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkInfoCallout
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkProfileButton
import com.invex.sportkid.core.designsystem.component.SkTonalButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.designsystem.R as DsR

private val ScanFrame = 224.dp

/**
 * P18. Unirse con código (plantilla T4): visor para escanear el QR del otro tutor, linterna y
 * QR desde la galería, o el código de 8 caracteres escrito a mano con su validación.
 *
 * @param status resultado de la última validación; si no es [InviteStatus.VALID] se muestra el
 * error de README §5.6 bajo el campo. Se limpia con [onCodeChange].
 * @param onScan abre el escáner de la cámara (el visor es una vista previa ilustrativa).
 */
@Composable
fun JoinCodeScreen(
    onBack: () -> Unit,
    onValidate: (code: String) -> Unit,
    modifier: Modifier = Modifier,
    status: InviteStatus? = null,
    onCodeChange: () -> Unit = {},
    onScan: () -> Unit = {},
    onFlashlight: () -> Unit = {},
    onGallery: () -> Unit = {},
    onRequestEmailInvite: () -> Unit = {},
    onHelp: () -> Unit = {},
    onProfile: () -> Unit = {},
    initialCode: String = "",
) {
    var code by rememberSaveable { mutableStateOf(InviteCode.normalize(initialCode)) }
    val colors = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = colors.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.join_title), onBack = onBack) {
                SkHelpButton(onClick = onHelp)
                SkProfileButton(onClick = onProfile)
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = SportKidTheme.spacing.screenMargin)
                .padding(top = SportKidTheme.spacing.sm, bottom = SportKidTheme.spacing.xxl),
        ) {
            Intro()
            Scanner(onScan, Modifier.padding(top = SportKidTheme.spacing.lg))
            Row(Modifier.padding(top = SportKidTheme.spacing.md), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
                ScannerAction(stringResource(R.string.join_flashlight), R.drawable.ic_join_flashlight, onFlashlight, Modifier.weight(1f))
                ScannerAction(stringResource(R.string.join_gallery), R.drawable.ic_join_gallery, onGallery, Modifier.weight(1f))
            }
            OrDivider(Modifier.padding(vertical = 20.dp))
            CodeCard(
                code = code,
                onCodeChange = {
                    code = InviteCode.normalize(it)
                    onCodeChange()
                },
                status = status,
                onValidate = { onValidate(code) },
            )
            WhereToFind(Modifier.padding(top = SportKidTheme.spacing.lg))
            TroubleRow(onRequestEmailInvite, Modifier.padding(top = SportKidTheme.spacing.lg))
        }
    }
}

/** Overline "Acceso familiar" con icono y la explicación de cómo unirse. */
@Composable
private fun Intro() {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkIconCircle(
                icon = painterResource(R.drawable.ic_join_group),
                contentDescription = null,
                size = 24.dp,
                containerColor = colors.secondaryContainer,
                iconTint = colors.secondary,
            )
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Text(
                stringResource(R.string.join_overline).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
                color = colors.primary,
            )
        }
        Text(stringResource(R.string.join_intro), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
    }
}

/** Visor oscuro con marco de escaneo, línea animada y la indicación para el otro tutor. */
@Composable
private fun Scanner(onScan: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val description = stringResource(R.string.join_scanner_cd)
    Box(
        modifier
            .fillMaxWidth()
            .shadow(SkElevation.level2, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(colors.inverseSurface)
            .clickable(role = Role.Button, onClickLabel = description, onClick = onScan)
            .semantics { contentDescription = description }
            .padding(vertical = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painterResource(R.drawable.img_sample_scan_preview),
            contentDescription = null,
            modifier = Modifier.matchParentSize().alpha(0.4f),
            contentScale = ContentScale.Crop,
        )
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.45f)))
        Box(
            Modifier
                .size(ScanFrame)
                .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        ) {
            // Marco de Figma con su azul original: el visor siempre es oscuro, no cambia con el tema.
            Icon(painterResource(R.drawable.ic_join_viewfinder), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.fillMaxSize())
            ScanLine(Modifier.align(Alignment.TopCenter))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkPill(
                    stringResource(R.string.join_scanner_searching),
                    containerColor = colors.primary.copy(alpha = 0.85f),
                    contentColor = colors.onPrimary,
                    leading = { Box(Modifier.size(6.dp).background(colors.surface, CircleShape)) },
                )
                Spacer(Modifier.weight(1f))
                Icon(painterResource(R.drawable.ic_join_fullscreen), contentDescription = null, tint = colors.surface)
            }
            Text(
                stringResource(R.string.join_scanner_hint),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 26.dp, vertical = 14.dp)
                    .background(Color.Black.copy(alpha = 0.4f), MaterialTheme.shapes.small)
                    .padding(horizontal = SportKidTheme.spacing.md, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.surface.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Línea de lectura que recorre el marco (Figma: degradado `primaryContainer` con halo). */
@Composable
private fun ScanLine(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scanLine")
    val fraction by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1_600, easing = LinearEasing), RepeatMode.Reverse),
        label = "scanLineY",
    )
    val line = MaterialTheme.colorScheme.primaryContainer
    Box(
        modifier
            .offset(y = ScanFrame * fraction)
            .padding(horizontal = 14.dp)
            .fillMaxWidth()
            .height(2.dp)
            .background(Brush.horizontalGradient(listOf(line.copy(alpha = 0f), line, line.copy(alpha = 0f)))),
    )
}

/** Botón tonal bajo el visor (Linterna, Subir galería). */
@Composable
private fun ScannerAction(text: String, icon: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SkTonalButton(
        text,
        onClick = onClick,
        modifier = modifier,
        icon = painterResource(icon),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        size = SkButtonSize.Medium,
    )
}

/** Separador "O escribe el código" entre el escáner y el campo manual. */
@Composable
private fun OrDivider(modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.surfaceContainerHigh
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
        Box(Modifier.weight(1f).height(2.dp).background(line, MaterialTheme.shapes.full))
        Text(stringResource(R.string.join_or_type), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.weight(1f).height(2.dp).background(line, MaterialTheme.shapes.full))
    }
}

/** "K7QM4PZX" → "K7QM-4PZX" al escribir, sin guardar el guion. */
private object InviteCodeTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val out = InviteCode.format(raw)
        return TransformedText(
            AnnotatedString(out),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = if (offset > 4) offset + 1 else offset
                override fun transformedToOriginal(offset: Int) = if (offset > 4) (offset - 1).coerceAtMost(raw.length) else offset
            },
        )
    }
}

/** Card del código: campo centrado, vigencia o error y "Validar código". */
@Composable
private fun CodeCard(code: String, onCodeChange: (String) -> Unit, status: InviteStatus?, onValidate: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val complete = code.length == InviteCode.LENGTH
    val error = when (status) {
        InviteStatus.EXPIRED -> R.string.join_error_expired
        InviteStatus.USED -> R.string.join_error_used
        InviteStatus.INVALID -> R.string.join_error_invalid
        InviteStatus.VALID, null -> null
    }?.let { stringResource(it) }
    val label = stringResource(R.string.join_code_label)
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.surfaceContainerLowest, shadowElevation = SkElevation.card) {
        Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Text(
                    stringResource(R.string.join_code_length, InviteCode.LENGTH),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
            val codeStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, textAlign = TextAlign.Center)
            BasicTextField(
                value = code,
                onValueChange = onCodeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = label
                        if (error != null) error(error)
                    },
                singleLine = true,
                textStyle = codeStyle.copy(color = if (error != null) colors.error else colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                visualTransformation = InviteCodeTransformation,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { if (complete) onValidate() }),
                decorationBox = { field ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
                            .padding(horizontal = SportKidTheme.spacing.lg),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (code.isEmpty()) {
                            Text(stringResource(R.string.join_code_placeholder), style = codeStyle, color = colors.outlineVariant)
                        }
                        field()
                    }
                },
            )
            Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(if (error != null) DsR.drawable.sk_ic_info else R.drawable.ic_join_shield),
                    contentDescription = null,
                    tint = if (error != null) colors.error else colors.primary,
                )
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                Text(
                    error ?: stringResource(R.string.join_code_help, InviteCode.VALIDITY_HOURS),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (error != null) colors.error else colors.onSurfaceVariant,
                )
            }
            SkButton(
                stringResource(R.string.join_validate),
                onClick = onValidate,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                enabled = complete,
                size = SkButtonSize.Medium,
                trailingIcon = painterResource(R.drawable.ic_join_arrow_forward),
            )
        }
    }
}

/** Callout "¿Dónde encuentro este código?" con el botón a buscar resaltado. */
@Composable
private fun WhereToFind(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val button = stringResource(R.string.join_where_button)
    val body = stringResource(R.string.join_where_body, button)
    val start = body.indexOf(button)
    SkInfoCallout(
        text = buildAnnotatedString {
            append(body)
            if (start >= 0) addStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.onSurface), start, start + button.length)
        },
        modifier = modifier,
        title = stringResource(R.string.join_where_title),
        containerColor = colors.surfaceContainerLow,
        contentColor = colors.onSurface,
        bodyColor = colors.onSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
        spacing = 14.dp,
        leading = {
            SkIconCircle(
                icon = painterResource(R.drawable.ic_join_lightbulb),
                contentDescription = null,
                containerColor = colors.tertiaryContainer,
                iconTint = colors.onTertiaryContainer,
            )
        },
    )
}

/** Fila "¿Problemas al escanear?" que pide una invitación por correo. */
@Composable
private fun TroubleRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.secondaryContainer.copy(alpha = 0.5f),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SkIconCircle(
                icon = painterResource(R.drawable.ic_join_support),
                contentDescription = null,
                size = 32.dp,
                containerColor = colors.secondaryContainer,
                iconTint = colors.secondary,
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.join_trouble_title), style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
                Text(stringResource(R.string.join_trouble_body), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Icon(painterResource(DsR.drawable.sk_ic_chevron_right), contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

@SkPreviews
@Composable
private fun JoinCodeScreenPreview() = SportKidTheme {
    JoinCodeScreen(onBack = {}, onValidate = {})
}

@SkPreviews
@Composable
private fun JoinCodeScreenErrorPreview() = SportKidTheme {
    JoinCodeScreen(onBack = {}, onValidate = {}, status = InviteStatus.INVALID, initialCode = "K7QM4PZA")
}
