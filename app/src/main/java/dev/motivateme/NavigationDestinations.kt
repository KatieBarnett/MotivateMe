package dev.motivateme

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object Home : NavKey

@Serializable
data class QuotesDestination(val topicName: String) : NavKey
