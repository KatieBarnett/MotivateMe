package dev.motivateme.data

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import dev.motivateme.BuildConfig
import dev.motivateme.models.Quote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiInterface @Inject constructor() {
    private val generativeModel by lazy {
        if (BuildConfig.IS_GEMINI_ENABLED) {
            Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash")
        } else {
            null
        }
    }

    suspend fun getQuote(topicName: String): Quote? {
        val response =
            generativeModel?.generateContent(
                "Give me a single motivational quotes on the topic of $topicName. " +
                    "Here are some examples of the style I want: " +
                        "${sampleData.firstOrNull { it.name == topicName }?.quotes?.map { it.text }}"
            )
        return response?.text?.let { Quote(it) }
    }
}
