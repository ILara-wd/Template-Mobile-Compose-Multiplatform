package com.akl.templatemobile.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.akl.hashshafiles.components.boldStyled
import com.akl.hashshafiles.components.italicStyled
import com.akl.hashshafiles.components.lineThroughStyled
import com.akl.hashshafiles.components.semiBoldStyled
import com.akl.hashshafiles.components.underlineStyled
import com.akl.templatemobile.Greeting
import com.akl.templatemobile.components.PrimaryButton
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import templatemobile.composeapp.generated.resources.Res
import templatemobile.composeapp.generated.resources.compose_multiplatform

// Creates routes
@Serializable
object Home

@Serializable
object Profile

@Serializable
object FriendsList

@Composable
fun AppNavigation() {
    // Creates the NavController
    val navController = rememberNavController()

// Creates the NavHost with the navigation graph consisting of supplied destinations
    NavHost(
        navController = navController,
        startDestination = Home
    ) {
        composable<Home> {
            HomeScreen {
                navController.navigate(Profile)
            }
        }
        composable<Profile> { //backStackEntry -> val profile: Profile = backStackEntry.toRoute()
            ProfileScreen {
                navController.navigate(FriendsList)
            }
        }
        composable<FriendsList> {
            FriendsListScreen {
                navController.popBackStack()
            }
        }
    }
}

@Composable
fun FriendsListScreen(onClick: () -> Unit) {
    ScreenMain {
        Column(
            modifier = Modifier.background(Color.LightGray).fillMaxSize()
        ) {
            Text("FriendsListScreen")
            PrimaryButton(text = "back") { onClick() }
            PrimaryButton(text = "boldStyled".boldStyled()) { onClick() }
            PrimaryButton(text = "semiBoldStyled".semiBoldStyled()) { onClick() }
            PrimaryButton(text = "italicStyled".italicStyled()) { onClick() }
            PrimaryButton(text = "underlineStyled".underlineStyled()) { onClick() }
            PrimaryButton(text = "lineThroughStyled".lineThroughStyled()) { onClick() }
        }
    }
}

@Composable
fun ProfileScreen(onClick: () -> Unit) {
    ScreenMain {
        Column(
            modifier = Modifier.background(Color.Cyan).fillMaxSize()
        ) {
            Text("ProfileScreen")
            PrimaryButton(
                text = "Go to profile".lineThroughStyled(),
                onClick = { onClick() }
            )
        }
    }
}


@Composable
fun HomeScreen(onClick: () -> Unit) {
    var showContent by remember { mutableStateOf(false) }
    ScreenMain {
        Button(onClick = { onClick() }) {
            Text("Navegar!")
        }
        Button(onClick = { showContent = !showContent }) {
            Text("Click me!")
        }
        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: $greeting")
            }
        }
    }

}

@Composable
fun ScreenMain(content: @Composable ColumnScope.() -> Unit) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content() }
    }
}
