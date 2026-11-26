package com.invex.one.template.core.designsystem.token

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.invex.one.template.core.designsystem.R

/** Font families bundled in `res/font` (static instances, so they work on minSdk 24). */
object AppFonts {
    val Outfit = FontFamily(
        Font(R.font.outfit_semibold, FontWeight.SemiBold),
        Font(R.font.outfit_bold, FontWeight.Bold),
        Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
    )
    val PlusJakartaSans = FontFamily(
        Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
        Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    )
    val SpaceGrotesk = FontFamily(Font(R.font.space_grotesk_bold, FontWeight.Bold))
}

/** CSS-like line height: text is centered in its line box and not trimmed. */
private val CenteredLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun style(
    family: FontFamily,
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    letterSpacing: Double = 0.0,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = CenteredLineHeight,
)

/** Text styles from Figma. Names describe the role, not the screen where they appear. */
@Immutable
data class AppTypography(
    /** Outfit ExtraBold 32/40 -0.02em — hero greetings. */
    val display: TextStyle = style(AppFonts.Outfit, FontWeight.ExtraBold, 32, 40, -0.64),
    /** Outfit Bold 28/36 -0.01em — screen headlines. */
    val headline: TextStyle = style(AppFonts.Outfit, FontWeight.Bold, 28, 36, -0.28),
    /** Outfit Bold 22/28 -0.025em — profile names. */
    val titleLarge: TextStyle = style(AppFonts.Outfit, FontWeight.Bold, 22, 28, -0.55),
    /** Outfit SemiBold 18/24 — screen, section and card titles. */
    val title: TextStyle = style(AppFonts.Outfit, FontWeight.SemiBold, 18, 24),
    /** Plus Jakarta Sans Bold 16/22 — emphasized body / highlight titles. */
    val subtitle: TextStyle = style(AppFonts.PlusJakartaSans, FontWeight.Bold, 16, 22),
    /** Plus Jakarta Sans Medium 16/22 — large input text. */
    val bodyLarge: TextStyle = style(AppFonts.PlusJakartaSans, FontWeight.Medium, 16, 22),
    /** Plus Jakarta Sans Medium 14/20 — body copy and input text. */
    val body: TextStyle = style(AppFonts.PlusJakartaSans, FontWeight.Medium, 14, 20),
    /** Plus Jakarta Sans Medium 14 +0.1em — revealed passwords / secrets. */
    val secret: TextStyle = style(AppFonts.PlusJakartaSans, FontWeight.Medium, 14, 20, 1.4),
    /** Plus Jakarta Sans Medium 12/16 — supporting text. */
    val bodySmall: TextStyle = style(AppFonts.PlusJakartaSans, FontWeight.Medium, 12, 16),
    /** Plus Jakarta Sans Bold 12/16 — emphasized supporting text. */
    val bodySmallStrong: TextStyle = style(AppFonts.PlusJakartaSans, FontWeight.Bold, 12, 16),
    /** Plus Jakarta Sans Bold 10/20 — tiny badges (XP). */
    val micro: TextStyle = style(AppFonts.PlusJakartaSans, FontWeight.Bold, 10, 20),
    /** Outfit Bold 18/24 +0.025em — large accent CTAs. */
    val buttonLarge: TextStyle = style(AppFonts.Outfit, FontWeight.Bold, 18, 24, 0.45),
    /** Space Grotesk Bold 14/18 +0.02em — buttons. */
    val button: TextStyle = style(AppFonts.SpaceGrotesk, FontWeight.Bold, 14, 18, 0.28),
    /** Space Grotesk Bold 11/14 +0.04em — chips, tags, nav labels, links. */
    val label: TextStyle = style(AppFonts.SpaceGrotesk, FontWeight.Bold, 11, 14, 0.44),
    /** Space Grotesk Bold 11/14 +0.05em — uppercase eyebrow labels. */
    val overline: TextStyle = style(AppFonts.SpaceGrotesk, FontWeight.Bold, 11, 14, 0.55),
    /** Space Grotesk Bold 20/24 -0.03em — big numeric values (scores). */
    val numeric: TextStyle = style(AppFonts.SpaceGrotesk, FontWeight.Bold, 20, 24, -0.6),
    /** Space Grotesk Bold 12/16 — numeric suffixes such as multipliers. */
    val numericSmall: TextStyle = style(AppFonts.SpaceGrotesk, FontWeight.Bold, 12, 16),
)
