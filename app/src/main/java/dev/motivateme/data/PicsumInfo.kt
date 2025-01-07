package dev.motivateme.data

import kotlinx.serialization.Serializable

@Serializable
data class PicsumInfo(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
)
