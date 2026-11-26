package com.invex.one.template.feature.auth.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.invex.one.template.core.designsystem.atom.IconCircle
import com.invex.one.template.core.designsystem.atom.TonalIconButton
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.organism.AppBarContainer
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppSize
import com.invex.one.template.feature.auth.R

/** Header shared by login and registration: back, "MINIJUEGOS" brand pill and a profile mark. */
@Composable
internal fun AuthTopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    AppBarContainer(modifier) {
        TonalIconButton(AppIcons.ArrowBack, stringResource(R.string.login_back), onClick = onBack)
        Row(
            modifier = Modifier
                .background(colors.surfaceContainerLow, AppTheme.shapes.pill)
                .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconCircle(AppIcons.GamepadBrand, colors.primary, AppSize.brandMark)
            Text(stringResource(R.string.login_brand).uppercase(), style = AppTheme.typography.overline, color = colors.onSurface)
        }
        Spacer(Modifier.weight(1f))
        IconCircle(AppIcons.Person, colors.primary, AppSize.avatar)
    }
}
