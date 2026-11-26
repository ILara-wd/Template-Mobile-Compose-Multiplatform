package com.invex.one.template.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.invex.one.template.core.designsystem.theme.AppTheme

enum class TagSize { Medium, Small }

/** Uppercase category tag (e.g. "PUZLE NUMÉRICO"). `Medium` pads 10dp, `Small` 8dp. */
@Composable
fun CategoryTag(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: TagSize = TagSize.Medium,
) {
    PillLabel(
        text = text.uppercase(),
        containerColor = containerColor,
        contentColor = contentColor,
        style = AppTheme.typography.label,
        horizontalPadding = if (size == TagSize.Medium) AppTheme.spacing.smd else AppTheme.spacing.sm,
        modifier = modifier,
    )
}

/** Small reward / status badge (e.g. "+150 XP"). */
@Composable
fun InlineBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    PillLabel(
        text = text,
        containerColor = containerColor,
        contentColor = contentColor,
        style = AppTheme.typography.micro,
        horizontalPadding = AppTheme.spacing.xs,
        modifier = modifier,
    )
}

@Composable
private fun PillLabel(
    text: String,
    containerColor: Color,
    contentColor: Color,
    style: TextStyle,
    horizontalPadding: Dp,
    modifier: Modifier,
) {
    Text(
        text = text,
        style = style,
        color = contentColor,
        maxLines = 1,
        modifier = modifier
            .background(containerColor, AppTheme.shapes.pill)
            .padding(horizontal = horizontalPadding, vertical = AppTheme.spacing.xxxs),
    )
}

@Preview
@Composable
private fun TagPreview() {
    AppTheme {
        CategoryTag("Puzle numérico", AppTheme.colors.tertiaryFixed, AppTheme.colors.onTertiaryFixed)
    }
}
