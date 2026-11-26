package com.invex.one.template.core.designsystem.token

import androidx.compose.ui.graphics.Color

/**
 * Primitive color tokens (raw palette) taken from the Figma file "Prototipo-Demo-UiKit".
 *
 * Components must NEVER reference these directly: use the semantic roles in [AppColors]
 * through `AppTheme.colors`. The palette only exists to feed the semantic layer.
 */
internal object ColorPalette {
    // Green (primary)
    val Green10 = Color(0xFF002109)
    val Green20 = Color(0xFF004B1E)
    val Green30 = Color(0xFF005321)
    val Green40 = Color(0xFF006E2F)
    val Green60 = Color(0xFF22C55E)
    val Green80 = Color(0xFF6BFF8F)

    // Blue (secondary)
    val Blue10 = Color(0xFF001E2C)
    val Blue30 = Color(0xFF004C69)
    val Blue40 = Color(0xFF00668A)
    val Blue60 = Color(0xFF2EB7F2)
    val Blue90 = Color(0xFFC4E7FF)

    // Orange (tertiary)
    val Orange10 = Color(0xFF2A1700)
    val Orange28 = Color(0xFF653E00)
    val Orange30 = Color(0xFF684000)
    val Orange40 = Color(0xFF855300)
    val Orange70 = Color(0xFFFEA619)
    val Orange90 = Color(0xFFFFDDB8)

    // Indigo neutrals (surfaces)
    val Indigo10 = Color(0xFF131B2E)
    val Indigo90 = Color(0xFFDAE2FD)
    val Indigo92 = Color(0xFFE2E7FF)
    val Indigo94 = Color(0xFFEAEDFF)
    val Indigo96 = Color(0xFFF2F3FF)
    val Indigo98 = Color(0xFFFAF8FF)

    // Green-grey neutral variant
    val NeutralVariant30 = Color(0xFF3D4A3D)

    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
}
