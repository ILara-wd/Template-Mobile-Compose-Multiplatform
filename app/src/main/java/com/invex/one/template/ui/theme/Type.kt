package com.invex.one.template.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Figma uses Inter; until the font is bundled we fall back to the platform sans-serif (Roboto).
private val VuelveFontFamily = FontFamily.Default

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Double) = TextStyle(
    fontFamily = VuelveFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

val Typography = Typography(
    // "Vuelve" wordmark
    displaySmall = style(30, 36, FontWeight.Bold, -0.75),
    // Form headings ("Crea tu cuenta de negocio")
    headlineSmall = style(22, 28, FontWeight.SemiBold, -0.55),
    // Top bar title
    titleLarge = style(18, 24, FontWeight.SemiBold, -0.45),
    // "Te damos la bienvenida"
    titleMedium = style(16, 22, FontWeight.SemiBold, -0.08),
    bodyMedium = style(14, 20, FontWeight.Normal, 0.0),
    bodySmall = style(12, 16, FontWeight.Normal, 0.12),
    // Buttons
    labelLarge = style(14, 20, FontWeight.SemiBold, 0.14),
    // Field labels, tabs, card titles
    labelMedium = style(12, 16, FontWeight.SemiBold, 0.24),
    // Links, divider text, metric pill
    labelSmall = style(11, 14, FontWeight.SemiBold, 0.33),
)
