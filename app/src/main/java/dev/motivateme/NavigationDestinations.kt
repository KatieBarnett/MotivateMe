package dev.motivateme

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class QuotesDestination(val topicName: String)