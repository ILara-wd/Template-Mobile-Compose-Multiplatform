package com.invex.one.template.core.designsystem.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.invex.one.template.core.designsystem.atom.IconCircle
import com.invex.one.template.core.designsystem.atom.TonalIconButton
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppSize

/**
 * Secondary-screen header: back button, brand pill and profile mark (auth, profile…).
 * Pass `onBack = null` on root screens to hide the back button.
 */
@Composable
fun BrandTopBar(
    brandName: String,
    backContentDescription: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    AppBarContainer(modifier) {
        onBack?.let { TonalIconButton(AppIcons.ArrowBack, backContentDescription, onClick = it) }
        Row(
            modifier = Modifier
                .background(colors.surfaceContainerLow, AppTheme.shapes.pill)
                .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconCircle(AppIcons.GamepadBrand, colors.primary, AppSize.brandMark)
            Text(brandName.uppercase(), style = AppTheme.typography.overline, color = colors.onSurface)
        }
        Spacer(Modifier.weight(1f))
        IconCircle(AppIcons.Person, colors.primary, AppSize.avatar)
    }
}
