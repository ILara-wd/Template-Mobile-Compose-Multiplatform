package com.invex.sportkid.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.R

/** Plus Jakarta Sans empaquetada en res/font (si un peso faltara, Android usa Roboto). */
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
)

private val Base = Typography()

private fun style(size: Int, weight: FontWeight, lineHeight: Int, letterSpacing: Float = 0f) =
    TextStyle(
        fontFamily = PlusJakartaSans,
        fontSize = size.sp,
        fontWeight = weight,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp,
    )

/** Escala tipográfica (README §1.3). Ningún feature usa tamaños sueltos. */
val SportKidTypography = Typography(
    displayLarge = Base.displayLarge.copy(fontFamily = PlusJakartaSans),
    displayMedium = Base.displayMedium.copy(fontFamily = PlusJakartaSans),
    displaySmall = style(36, FontWeight.Bold, 44),
    headlineLarge = Base.headlineLarge.copy(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold),
    headlineMedium = Base.headlineMedium.copy(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold),
    headlineSmall = style(24, FontWeight.Bold, 32),
    titleLarge = style(22, FontWeight.Bold, 28),
    titleMedium = style(16, FontWeight.SemiBold, 24),
    titleSmall = style(14, FontWeight.SemiBold, 20),
    bodyLarge = style(16, FontWeight.Normal, 24),
    bodyMedium = style(14, FontWeight.Normal, 20),
    bodySmall = style(12, FontWeight.Normal, 16),
    labelLarge = style(14, FontWeight.SemiBold, 20),
    labelMedium = style(12, FontWeight.SemiBold, 16),
    // Overline de sección: el texto se pasa a MAYÚSCULAS en SkOverline.
    labelSmall = style(11, FontWeight.SemiBold, 16, letterSpacing = 0.5f),
)

/**
 * Extensión `metric`: tiempos (`00:41.23`), montos y distancias.
 * Usa cifras tabulares (`tnum`) para que los dígitos no "bailen" en tablas.
 */
val SportKidMetric: TextStyle = style(20, FontWeight.Bold, 24).copy(fontFeatureSettings = "tnum")

@Suppress("UnusedReceiverParameter")
val Typography.metric: TextStyle get() = SportKidMetric
