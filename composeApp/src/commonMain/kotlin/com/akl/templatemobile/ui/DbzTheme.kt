package com.akl.templatemobile.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** * DarkColorScheme defines the dark color scheme for the Template Multiplatform.
 * It uses the primary, secondary, and tertiary colors from the ColorSegmentation.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD7423B),
    secondary = Color(0xFFA82C1F),
    tertiary = Color(0xFF71120D)
)

/** * LightColorScheme defines the light color scheme for the Template Multiplatform.
 * It uses the primary, secondary, and tertiary colors from the ColorSegmentation.
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFA9256),
    secondary = Color(0xFFB6602F),
    tertiary = Color(0xFF944A1F)
)

/** * DbzTheme is the main theme for the Template Multiplatform
 * It provides a consistent look and feel across the application.
 *
 * @param darkTheme Boolean flag to indicate whether to use the dark theme. Defaults to system setting.
 * @param content The content to be displayed within the theme.
 * @see [LightColorScheme] and [DarkColorScheme] for color schemes.
 */
@Composable
fun DbzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
