package dev.motivateme.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface ImageWidgetState {
    @Serializable
    data object Loading : ImageWidgetState

    @Serializable
    data class Available(
        val imageUrl: String,
        val imageAuthor: String? = null,
        val downloadedImageFilePath: String? = null,
    ) : ImageWidgetState

    @Serializable
    data class Unavailable(val message: String) : ImageWidgetState
}