package com.invex.one.demo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private fun inter(
    size: Int,
    lineHeight: Float,
    weight: FontWeight = FontWeight.SemiBold,
    letterSpacing: Float = 0f,
) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

/** Escala tipográfica del diseño (Inter). */
val Typography = Typography(
    // H1 "Buenos días, Carlos"
    headlineSmall = inter(24, 32f, letterSpacing = -0.24f),
    // Cifra de métrica
    titleLarge = inter(20, 25f),
    // Títulos de sección (H2)
    titleMedium = inter(16, 22f),
    // Nombre de cliente / títulos de tarjeta
    titleSmall = inter(14, 20f),
    bodyMedium = inter(14, 20f, FontWeight.Normal),
    bodySmall = inter(12, 16f, FontWeight.Normal),
    // Botones
    labelLarge = inter(14, 20f),
    labelMedium = inter(12, 16f, letterSpacing = 0.24f),
    // Badges, metadatos, navegación
    labelSmall = inter(11, 14f, letterSpacing = 0.44f),
)
