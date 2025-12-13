package com.akl.templatemobile.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akl.hashshafiles.components.StyledText
import com.akl.hashshafiles.components.boldStyled
import com.akl.templatemobile.core.network.ApiClient.clientApiHttp
import com.akl.templatemobile.core.network.DragonBallService
import com.akl.templatemobile.viewmodel.HomeViewModel
import com.seiko.imageloader.rememberAsyncImagePainter

@Composable
fun DetailScreen(id: Int, navigateBack: () -> Unit) {
    val client = remember { clientApiHttp() }
    val viewModel = viewModel { HomeViewModel(service = DragonBallService(client = client)) }
    viewModel.getCharacterById(id = id)
    Scaffold { paddingValues ->
        val items = viewModel.uiStateDetail.character
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Personaje seleccionado: ${items?.name}", fontSize = 25.sp)
            if (!items?.transformations.isNullOrEmpty()) {
                StyledText(
                    text = "Transofmaciones de ${items.name}".boldStyled(),
                    style = TextStyle(fontSize = 30.sp)
                )
                LazyRow {
                    items(items.transformations.size) { transformation ->
                        Column(
                            modifier = Modifier
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val image = items.transformations[transformation].image
                            Image(
                                painter = rememberAsyncImagePainter(image),
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(240.dp)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                }
            } else {
                Text("No hay transformaciones disponibles de ${items?.name}", fontSize = 20.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navigateBack() }) {
                Text("Atrás")
            }
        }
    }
}