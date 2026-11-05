package com.invex.one.template.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.template.R
import com.invex.one.template.ui.theme.VuelveBackground
import com.invex.one.template.ui.theme.VuelveOnPrimaryContainer
import com.invex.one.template.ui.theme.VuelveOnSurface
import com.invex.one.template.ui.theme.VuelveOnSurfaceVariant
import com.invex.one.template.ui.theme.VuelvePrimary
import com.invex.one.template.ui.theme.VuelvePrimaryContainer

/** The four persistent destinations of the bottom navigation (README O3). */
enum class MainTab(@StringRes val label: Int, @DrawableRes val icon: Int) {
    Home(R.string.nav_home, R.drawable.ic_nav_home),
    Clients(R.string.nav_clients, R.drawable.ic_nav_clients),
    FollowUps(R.string.nav_follow_ups, R.drawable.ic_nav_followups),
    Results(R.string.nav_results, R.drawable.ic_nav_results),
}

private val BarBackground = VuelveBackground.copy(alpha = 0.9f)

/**
 * Frame for the main (tabbed) screens. Bars are translucent and drawn over the content,
 * so screens must apply the provided [PaddingValues] *inside* their scroll container.
 */
@Composable
fun VuelveMainScaffold(
    title: String,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    accountAvatar: Painter,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = VuelveBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { VuelveAppTopBar(title = title, accountAvatar = accountAvatar, onAccountClick = onAccountClick) },
        bottomBar = { VuelveBottomBar(selected = selectedTab, onSelect = onTabSelected) },
        content = content,
    )
}

/** O1 — App bar: Vuelve logo, "VUELVE • PET RETENTION" overline + screen title, account avatar. */
@Composable
fun VuelveAppTopBar(
    title: String,
    accountAvatar: Painter,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.04f),
            )
            .background(BarBackground)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_vuelve_logo),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
                Column {
                    Text(
                        text = stringResource(R.string.vuelve_overline),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
                        color = VuelveOnSurfaceVariant,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(letterSpacing = (-0.18).sp),
                        color = VuelveOnSurface,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onAccountClick, role = Role.Button)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(
                    painter = accountAvatar,
                    contentDescription = stringResource(R.string.vuelve_account_menu),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                )
                Image(painter = painterResource(R.drawable.ic_home_chevron_down), contentDescription = null)
            }
        }
    }
}

/** O3 — Bottom navigation with a lavender pill indicator on the active destination. */
@Composable
fun VuelveBottomBar(selected: MainTab, onSelect: (MainTab) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f),
            )
            .background(BarBackground)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 4.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .selectable(selected = isSelected, onClick = { onSelect(tab) }, role = Role.Tab),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(32.dp)
                            .background(if (isSelected) VuelvePrimaryContainer else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(tab.icon),
                            contentDescription = null,
                            tint = if (isSelected) VuelveOnPrimaryContainer else VuelveOnSurfaceVariant,
                        )
                    }
                    Text(
                        text = stringResource(tab.label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) VuelvePrimary else VuelveOnSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}
