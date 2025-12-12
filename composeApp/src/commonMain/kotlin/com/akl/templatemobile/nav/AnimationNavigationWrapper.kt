package com.akl.templatemobile.nav

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.akl.templatemobile.core.back
import com.akl.templatemobile.core.navigateTo
import com.akl.templatemobile.nav.Routes.Detail
import com.akl.templatemobile.nav.Routes.Error
import com.akl.templatemobile.nav.Routes.Home
import com.akl.templatemobile.screen.DetailScreen
import com.akl.templatemobile.screen.HomeScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun AdvanceNavigationWrapper() {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Home::class, Home.serializer())
                    subclass(Detail::class, Detail.serializer())
                }
            }
        },
        Home
    )

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.back() },
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen { id ->
                    backStack.navigateTo(Detail(id))
                }
            }
            entry<Detail> { key ->
                DetailScreen(key.id) {
                    backStack.back()
                }
            }

            entry<Error> {
                Text("Error :(")
            }
        }
    )
}
