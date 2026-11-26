package com.invex.one.template.core.designsystem.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.BarTitle
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppAlpha
import com.invex.one.template.core.designsystem.token.AppSize

/**
 * Translucent 64dp header container. Draws behind the status bar (edge-to-edge) and pads
 * its content below it. Content is laid out in a row with 16dp horizontal padding.
 */
@Composable
fun AppBarContainer(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(AppTheme.spacing.sm),
    content: @Composable RowScope.() -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .appShadow(AppTheme.elevation.bar, RectangleShape, colors.shadow)
            .background(colors.surface.copy(alpha = AppAlpha.BarTop))
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppSize.topBarHeight)
                .padding(horizontal = AppTheme.spacing.lg),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/** App header: brand logo, app name + current section, trailing actions. */
@Composable
fun AppTopBar(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    AppBarContainer(modifier) {
        AppIcon(AppIcons.BrandLogo, Modifier.size(AppSize.logo))
        BarTitle(title = title, subtitle = subtitle, modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}
