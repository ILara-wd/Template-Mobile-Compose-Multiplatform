package com.akl.templatemobile.core.network

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akl.templatemobile.core.network.ApiClient.clientApiHttp
import com.akl.templatemobile.viewmodel.HomeViewModel
import com.seiko.imageloader.rememberAsyncImagePainter
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {
    fun clientApiHttp() = HttpClient {
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

@Composable
fun rememberApiClient(navigateToDetail: (Int) -> Unit) {
    val client = remember { clientApiHttp() }
    val viewModel = viewModel { HomeViewModel(service = DragonBallService(client = client)) }
    viewModel.getGreeting()
    val items = viewModel.uiState.greeting?.items.orEmpty()
    Scaffold { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            Modifier.padding(paddingValues)
        ) {
            items(items) { character ->
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .fillMaxWidth()
                        .clickable {
                            navigateToDetail(character.id)
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(character.image),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .width(120.dp)
                            .height(240.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Text(text = character.name)
                }
            }
        }
    }

}
