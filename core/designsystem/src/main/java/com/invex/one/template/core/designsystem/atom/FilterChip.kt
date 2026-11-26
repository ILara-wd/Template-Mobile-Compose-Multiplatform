package com.invex.one.template.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize

/** Single-choice filter pill (36dp). Selected: inverse (dark) fill. */
@Composable
fun AppFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.pill
    Row(
        modifier = modifier
            .height(AppSize.filterChipHeight)
            .then(if (selected) Modifier.appShadow(AppTheme.elevation.xs, shape, colors.shadow) else Modifier)
            .clip(shape)
            .background(if (selected) colors.onSurface else colors.surfaceContainerHigh)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            style = AppTheme.typography.button,
            color = if (selected) colors.background else colors.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun AppFilterChipPreview() {
    AppTheme {
        Row(Modifier.padding(AppTheme.spacing.lg), horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            AppFilterChip("Todos", selected = true, onClick = {})
            AppFilterChip("Puzle", selected = false, onClick = {})
        }
    }
}
