package dev.motivateme.models

data class Topic(
    val isGenerative: Boolean = false,
    val name: String,
    val quotes: List<Quote>
)
