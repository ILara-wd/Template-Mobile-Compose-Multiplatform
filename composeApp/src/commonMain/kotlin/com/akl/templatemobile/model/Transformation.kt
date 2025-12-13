package com.akl.templatemobile.model

import kotlinx.serialization.Serializable

@Serializable
data class Transformation(
    val id: Int,
    val image: String,
    val ki: String,
    val name: String
)
