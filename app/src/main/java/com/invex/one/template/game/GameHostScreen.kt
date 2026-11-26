package com.invex.one.template.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.invex.one.template.R
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppSize

/**
 * Full-screen container for a running game: back action + title, then the game itself.
 * No Figma design exists for this screen yet; it only uses design-system tokens.
 */
@Composable
fun GameHostScreen(
    gameId: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    val colors = AppTheme.colors
    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppSize.topBarHeight)
                .padding(horizontal = AppTheme.spacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.game_back),
                    tint = colors.onSurface,
                )
            }
            Text(
                title,
                style = AppTheme.typography.title,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            GameRegistry.content(gameId)?.invoke()
        }
    }
}
