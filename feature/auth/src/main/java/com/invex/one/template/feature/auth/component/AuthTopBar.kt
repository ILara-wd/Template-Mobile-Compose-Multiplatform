package com.invex.one.template.feature.auth.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.invex.one.template.core.designsystem.organism.BrandTopBar
import com.invex.one.template.feature.auth.R

/** Header shared by login and registration. */
@Composable
internal fun AuthTopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    BrandTopBar(
        brandName = stringResource(R.string.login_brand),
        backContentDescription = stringResource(R.string.login_back),
        onBack = onBack,
        modifier = modifier,
    )
}
