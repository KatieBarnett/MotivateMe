package dev.motivateme.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface WidgetState {
    @Serializable
    data object Loading : WidgetState

    @Serializable
    data class Available(
        val topicName: String,
        val quote: Quote,
    ) : WidgetState

    @Serializable
    data class Unavailable(val message: String) : WidgetState
}