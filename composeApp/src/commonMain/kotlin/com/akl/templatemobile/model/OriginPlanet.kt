package com.akl.templatemobile.model

import kotlinx.serialization.Serializable

@Serializable
data class OriginPlanet(
    val description: String,
    val id: Int,
    val image: String,
    val isDestroyed: Boolean,
    val name: String
)