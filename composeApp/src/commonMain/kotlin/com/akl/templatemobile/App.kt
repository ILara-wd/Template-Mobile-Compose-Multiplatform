package com.akl.templatemobile

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.akl.templatemobile.nav.AdvanceNavigationWrapper
import com.akl.templatemobile.ui.DbzTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = rememberSaveable { mutableStateOf(isSystemInDarkTheme) }
    DbzTheme(
        darkTheme = isDarkTheme.value
    ) {
        AdvanceNavigationWrapper()
    }
}
