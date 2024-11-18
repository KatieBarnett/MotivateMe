package dev.motivateme.data

import dev.motivateme.models.Quote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteDataSource @Inject constructor(
    private val geminiInterface: GeminiInterface
) {
    suspend fun getQuote(topicName: String): Quote? {
        return geminiInterface.getQuote(topicName) ?: sampleData.filter {
            it.name == topicName
        }.firstOrNull()?.quotes?.random()
    }
}
