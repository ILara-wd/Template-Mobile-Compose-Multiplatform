package com.invex.sportkid.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Preview obligatoria de cada componente del kit: claro, oscuro y fontScale 1.3.
 * Para revisar un tema MaterialKolor, envolver el contenido en
 * `SportKidTheme(palette = AppPalette.Dojang)` dentro de la preview.
 */
@Preview(name = "Claro", showBackground = true)
@Preview(
    name = "Oscuro",
    showBackground = true,
    backgroundColor = 0xFF111418,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Preview(name = "Fuente 1.3", showBackground = true, fontScale = 1.3f)
annotation class SkPreviews
