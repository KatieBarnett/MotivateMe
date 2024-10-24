package dev.motivateme.data

import com.google.ai.client.generativeai.GenerativeModel
import dev.motivateme.BuildConfig
import dev.motivateme.models.Quote
import dev.motivateme.models.Topic
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiInterface @Inject constructor() {
    private val apiKey get() = "YOUR_API_KEY"
    private val splittingDelimiter = "::"

    private val generativeModel by lazy {
        if (BuildConfig.IS_GEMINI_ENABLED) {
            GenerativeModel(
                modelName = "gemini-1.5-flash-002",
                apiKey = apiKey
            )
        } else {
            null
        }
    }

    suspend fun generateTopics(): List<Topic> {
        val response =
            generativeModel?.generateContent(
                "Give 3 topics for writing quotes about. Make sure they are 2 words each, capitalised. Separate each of them by `$splittingDelimiter` only."
            )
        val topics = response?.text?.split(splittingDelimiter)?.map {
            Topic(
                name = it.trim(),
                isGenerative = true,
                quotes = emptyList()
            )
        } ?: emptyList()
        return topics
    }

    suspend fun getQuotes(topicName: String): List<Quote> {
        val response =
            generativeModel?.generateContent(
                "Give me quotes on the topic of $topicName. I need at only 3 quotes. Separate each quote by `$splittingDelimiter` only."
            )
        val quotes = response?.text?.split(splittingDelimiter)?.map {
            Quote(text = it.trim())
        } ?: emptyList()
        return quotes
    }

    suspend fun getSingleQuote(topicName: String?): Quote? {
        if (topicName == null) return null
        val response =
            generativeModel?.generateContent(
                "Give me a single quote on the topic of $topicName. Do not use any religious quotes."
            )
        val quote = response?.text?.let {
            Quote(text = it.trim())
        }
        return quote
    }
}
