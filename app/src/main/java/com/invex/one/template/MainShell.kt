package com.invex.one.template

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.invex.sportkid.core.designsystem.component.SkMainTopBar
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme

/** Destinos de la bottom navigation (O3). */
enum class MainTab(@StringRes val label: Int, @DrawableRes val icon: Int) {
    Home(R.string.nav_home, R.drawable.ic_nav_home),
    Agenda(R.string.nav_agenda, R.drawable.ic_nav_agenda),
    Athletes(R.string.nav_athletes, R.drawable.ic_nav_athletes),
    Expenses(R.string.nav_expenses, R.drawable.ic_nav_expenses),
}

/** O3. Bottom navigation: 4 destinos, el activo en pill `secondaryContainer`. */
@Composable
fun SkBottomBar(selected: MainTab, onSelect: (MainTab) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(
        modifier = modifier,
        containerColor = colors.surfaceContainer,
        tonalElevation = SkElevation.level0,
    ) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                label = { Text(stringResource(tab.label), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.onSecondaryContainer,
                    selectedTextColor = colors.onSurface,
                    indicatorColor = colors.secondaryContainer,
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant,
                ),
            )
        }
    }
}

/**
 * T1. Plantilla principal: top bar (O1), contenido del tab y bottom navigation (O3).
 * Los tabs sin diseño todavía muestran un aviso.
 */
@Composable
fun MainShell(
    tab: MainTab,
    onTabChange: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    content: @Composable (MainTab, Modifier) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            SkMainTopBar(
                onNotificationsClick = {}, // TODO: centro de notificaciones.
                onProfileClick = onProfileClick,
                avatar = painterResource(R.drawable.img_sample_user_avatar),
                // Gastos (P16) muestra el nombre de la pestaña; las demás, la marca.
                title = if (tab == MainTab.Expenses) stringResource(tab.label) else null,
            )
        },
        bottomBar = { SkBottomBar(selected = tab, onSelect = onTabChange) },
    ) { padding ->
        content(tab, Modifier.padding(padding))
    }
}

/** Contenido provisional de los tabs sin diseño. */
@Composable
fun ComingSoon(tab: MainTab, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            stringResource(R.string.tab_coming_soon, stringResource(tab.label)),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(SportKidTheme.spacing.xl),
        )
    }
}

@SkPreviews
@Composable
private fun SkBottomBarPreview() = SportKidTheme {
    SkBottomBar(selected = MainTab.Agenda, onSelect = {})
}
