package com.invex.one.template.core.designsystem.molecule

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.invex.one.template.core.designsystem.theme.AppTheme

/** Title + supporting line. */
@Composable
fun HeadingBlock(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(title, style = AppTheme.typography.title, color = AppTheme.colors.onSurface)
        Text(subtitle, style = AppTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
    }
}

/** Compact title + eyebrow used in bars (title ellipsizes when space runs out). */
@Composable
fun BarTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            title,
            style = AppTheme.typography.title,
            color = AppTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(subtitle, style = AppTheme.typography.label, color = AppTheme.colors.onSurfaceVariant, maxLines = 1)
    }
}
