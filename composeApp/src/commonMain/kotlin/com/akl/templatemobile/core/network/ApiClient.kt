package com.akl.templatemobile.core.network

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akl.templatemobile.viewmodel.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient

@Composable
fun rememberApiClient(navigateToDetail: (String) -> Unit) {
    val client = remember {
        HttpClient {
            install(plugin = ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(plugin = DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "dragonball-api.com"
                    /** parameters.append("API_KEY", "API_KEY") */
                }
            }
        }
    }

    val viewModel = viewModel { HomeViewModel(service = DragonBallService(client = client)) }
    viewModel.getGreeting()
    val items = viewModel.uiState.greeting?.items.orEmpty()
    Scaffold { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            items(items) { character ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .height(50.dp)
                        .fillMaxWidth()
                        .clickable {
                            navigateToDetail(character.name)
                        }
                ) { Text(text = character.name) }
            }
        }
    }

}
