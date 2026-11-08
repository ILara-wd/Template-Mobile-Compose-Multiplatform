package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.R
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import kotlinx.coroutines.delay

/**
 * M23. Chip de código: icono, código con tracking ("K7QM-4PZX") y botón pill "Copiar". Copia
 * [code] al portapapeles y avisa con [onCopied] para que la pantalla muestre la confirmación.
 */
@Composable
fun CopyableCodeChip(
    code: String,
    onCopied: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = colors.surfaceContainer) {
        Row(
            Modifier.padding(horizontal = SportKidTheme.spacing.lg, vertical = SportKidTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.sk_ic_code), contentDescription = null, tint = colors.primary)
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Text(
                code,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 0.8.sp),
                color = colors.onSurface,
                maxLines = 1,
            )
            val copyDescription = stringResource(R.string.sk_code_copy_cd, code)
            Surface(
                onClick = {
                    clipboard.setText(AnnotatedString(code))
                    onCopied()
                },
                modifier = Modifier.semantics { contentDescription = copyDescription },
                shape = MaterialTheme.shapes.full,
                color = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer,
            ) {
                Row(
                    Modifier.padding(PaddingValues(horizontal = SportKidTheme.spacing.lg, vertical = 6.dp)),
                    horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.sk_ic_copy), contentDescription = null)
                    Text(stringResource(R.string.sk_code_copy), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private const val MinuteMillis = 60_000L
private const val HourMillis = 60 * MinuteMillis

/**
 * M24. Cuenta regresiva en pill `tertiaryContainer`: "Expira en 23:59 h". Se actualiza cada
 * minuto y cambia a `errorContainer` cuando falta menos de 1 h; al vencer muestra "Código expirado".
 *
 * @param expiresAtMillis instante de expiración en epoch millis.
 * @param now reloj inyectable para previews y pruebas.
 */
@Composable
fun CountdownText(
    expiresAtMillis: Long,
    modifier: Modifier = Modifier,
    now: () -> Long = System::currentTimeMillis,
) {
    var current by remember { mutableLongStateOf(now()) }
    LaunchedEffect(expiresAtMillis) {
        while (true) {
            current = now()
            if (current >= expiresAtMillis) break
            delay(MinuteMillis - (expiresAtMillis - current) % MinuteMillis)
        }
    }
    val remaining = (expiresAtMillis - current).coerceAtLeast(0)
    // Redondea hacia arriba al minuto para no mostrar 00:00 h mientras aún es válido.
    val minutesLeft = (remaining + MinuteMillis - 1) / MinuteMillis
    val colors = MaterialTheme.colorScheme
    val urgent = remaining < HourMillis
    val text = if (remaining == 0L) {
        stringResource(R.string.sk_countdown_expired)
    } else {
        stringResource(R.string.sk_countdown_expires_in, minutesLeft / 60, minutesLeft % 60)
    }
    SkPill(
        text = text,
        modifier = modifier,
        containerColor = if (urgent) colors.errorContainer else colors.tertiaryContainer,
        contentColor = if (urgent) colors.onErrorContainer else colors.onTertiaryContainer,
        icon = painterResource(R.drawable.sk_ic_timer),
        iconTint = if (urgent) colors.error else colors.tertiary,
        contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.lg, vertical = SportKidTheme.spacing.xs),
        textStyle = MaterialTheme.typography.labelMedium,
    )
}

@SkPreviews
@Composable
private fun SkShareCodePreview() = SportKidTheme {
    val now = 1_700_000_000_000L
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CopyableCodeChip(code = "K7QM-4PZX", onCopied = {})
        CountdownText(expiresAtMillis = now + 24 * HourMillis - MinuteMillis, now = { now })
        CountdownText(expiresAtMillis = now + 42 * MinuteMillis, now = { now })
        Spacer(Modifier.size(4.dp))
    }
}
