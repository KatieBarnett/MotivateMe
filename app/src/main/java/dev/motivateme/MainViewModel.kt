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
        if (BuildConfig.IS_GEMINI_ENABLED) {
            generateTopics()
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

    private fun generateTopics() {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.emit(true)
            val newTopics = geminiInterface.generateTopics()
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
