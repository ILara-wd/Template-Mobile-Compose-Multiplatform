package com.invex.one.demo.ui.components.organisms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.molecules.CpSectionShape
import com.invex.one.demo.ui.theme.CpInverseOnSurface
import com.invex.one.demo.ui.theme.CpInverseSurface
import kotlinx.coroutines.delay

/** Estado de un toast efímero: [show] reemplaza el mensaje visible y reinicia el temporizador. */
@Stable
class CpToastState {
    var message by mutableStateOf<String?>(null)
        private set
    internal var serial by mutableStateOf(0)
        private set

    fun show(text: String) {
        message = text
        serial++
    }

    internal fun dismiss() {
        message = null
    }
}

@Composable
fun rememberCpToastState() = remember { CpToastState() }

/**
 * Toast de retroalimentación instantánea (superficie inversa, radio 12dp) que
 * aparece sobre el borde inferior y se oculta tras [durationMillis].
 */
@Composable
fun CpToastHost(state: CpToastState, modifier: Modifier = Modifier, durationMillis: Long = 2500) {
    // Conserva el último texto para que no se vacíe durante la animación de salida.
    var lastMessage by remember { mutableStateOf("") }
    state.message?.let { lastMessage = it }
    LaunchedEffect(state.serial) {
        if (state.message != null) {
            delay(durationMillis)
            state.dismiss()
        }
    }
    AnimatedVisibility(
        visible = state.message != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier.navigationBarsPadding().padding(16.dp),
    ) {
        Row(
            Modifier
                .widthIn(max = 384.dp)
                .fillMaxWidth()
                .shadow(10.dp, CpSectionShape, ambientColor = Color.Black.copy(0.1f), spotColor = Color.Black.copy(0.1f))
                .background(CpInverseSurface, CpSectionShape)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CpIcon(R.drawable.ic_info_toast)
            Text(
                lastMessage,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = CpInverseOnSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
