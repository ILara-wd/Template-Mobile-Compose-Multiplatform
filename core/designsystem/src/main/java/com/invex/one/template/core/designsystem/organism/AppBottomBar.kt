package com.invex.one.template.core.designsystem.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.NavBarItem
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppAlpha
import com.invex.one.template.core.designsystem.token.AppSize

/** Translucent bottom navigation container. Put [NavBarItem]s in [content]. */
@Composable
fun AppBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .appShadow(AppTheme.elevation.bar, RectangleShape, colors.shadow)
            .background(colors.surface.copy(alpha = AppAlpha.BarBottom))
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppSize.bottomBarHeight)
                .padding(horizontal = AppTheme.spacing.lg)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Preview
@Composable
private fun AppBottomBarPreview() {
    AppTheme {
        AppBottomBar {
            NavBarItem(AppIcons.Home, "Inicio", selected = true, onClick = {})
            NavBarItem(AppIcons.Gamepad, "Juegos", selected = false, onClick = {})
            NavBarItem(AppIcons.Trophy, "Récords", selected = false, onClick = {})
        }
    }
}
