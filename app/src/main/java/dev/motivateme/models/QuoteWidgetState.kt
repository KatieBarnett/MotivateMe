package dev.motivateme.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface QuoteWidgetState {
    @Serializable
    data object Loading : QuoteWidgetState

    @Serializable
    data class Available(
        val topicName: String,
        val quote: Quote,
    ) : QuoteWidgetState

    @Serializable
    data class Unavailable(val message: String) : QuoteWidgetState
}