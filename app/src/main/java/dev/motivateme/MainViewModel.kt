package dev.motivateme

import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.motivateme.data.DataRepository
import dev.motivateme.data.GeminiInterface
import dev.motivateme.models.Quote
import dev.motivateme.models.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val geminiInterface: GeminiInterface
) : ViewModel() {

    val showLoading = MutableStateFlow(true)

    val topics = dataRepository.getTopics().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf()
    )

    private val _generatedTopics = MutableStateFlow<List<Topic>>(emptyList())
    val generatedTopics: StateFlow<List<Topic>> = _generatedTopics

    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes

    init {
        viewModelScope.launch {
            if (BuildConfig.IS_GEMINI_ENABLED) {
                if (!dataRepository.hasGeneratedTopics().first()) {
                    generateTopics()
                } else {
                    dataRepository.getGeneratedTopics().collect {
                        showLoading.emit(false)
                        _generatedTopics.emit(it)
                    }
                }
            } else {
                showLoading.emit(false)
            }
        }
    }

    fun getQuotes(topicName: String) {
        val quotes = topics.value.firstOrNull { it.name == topicName }?.quotes ?: emptyList()
        if (quotes.isEmpty()) {
            generateQuotes(topicName)
        } else {
            _quotes.value = quotes
        }
    }

    suspend fun getSingleQuote(topicName: String): Quote? {
        val quote = topics.value.firstOrNull { it.name == topicName }?.quotes?.firstOrNull()
        return quote ?: geminiInterface.getSingleQuote(topicName)
    }

    private fun generateTopics() {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.emit(true)
            val newTopics = geminiInterface.generateTopics()
            dataRepository.saveGeneratedTopics(newTopics)
            _generatedTopics.emit(newTopics)
            showLoading.emit(false)
        }
    }

    private fun generateQuotes(topicName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.emit(true)
            val newQuotes = geminiInterface.getQuotes(topicName)
            _quotes.emit(newQuotes)
            showLoading.emit(false)
        }
    }
}
