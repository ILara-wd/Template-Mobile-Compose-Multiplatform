package com.akl.templatemobile.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

class DragonBallService(
    val client: HttpClient
) {
    suspend fun getAllCharacters(): RemoteResult {
        return client
            .get("/characters?limit=60")
            .body<RemoteResult>()
    }
}

@Serializable
data class RemoteResult(
    val items: List<CharacterResponse>
)

@Serializable
data class CharacterResponse(
    val id: Int,
    val name: String,
    val ki: String,
    val maxKi: String,
    val race: String,
    val gender: String,
    val description: String,
    val image: String,
    val affiliation: String,
)
