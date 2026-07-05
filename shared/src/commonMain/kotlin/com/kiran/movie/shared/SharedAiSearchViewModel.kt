package com.kiran.movie.shared

import com.kiran.movie.data.models.Item
import com.kiran.movie.domain.repository.AiSearchParameters
import com.kiran.movie.domain.usecase.AiDiscoverUseCase
import com.kiran.movie.domain.usecase.AiSearchUseCase
import com.kiran.movie.domain.voice.SpeechRecognizer
import com.kiran.movie.domain.voice.SpeechState
import com.kiran.movie.domain.voice.VoiceSynthesizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedAiSearchViewModel(
    private val speechRecognizer: SpeechRecognizer,
    private val voiceSynthesizer: VoiceSynthesizer,
    private val aiSearchUseCase: AiSearchUseCase,
    private val aiDiscoverUseCase: AiDiscoverUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val speechState = speechRecognizer.state

    private val _results = MutableStateFlow<List<Item>>(emptyList())
    val results: StateFlow<List<Item>> = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _aiInterpretation = MutableStateFlow("")
    val aiInterpretation: StateFlow<String> = _aiInterpretation.asStateFlow()

    private val _latestParams = MutableStateFlow<AiSearchParameters?>(null)
    val latestParams: StateFlow<AiSearchParameters?> = _latestParams.asStateFlow()

    // Tracks the last recognized text to detect when speech finishes
    private var autoProcessJob: Job? = null
    private var lastSpeechText = ""

    init {
        // Seed from the current recognizer state so we never re-fire for
        // text that was recognized in a PREVIOUS screen visit (the SpeechRecognizer
        // is a singleton, its StateFlow holds the last result forever).
        lastSpeechText = speechRecognizer.state.value.text

        // Observe speech state — only react to NEW completions
        scope.launch {
            speechState.collect { state ->
                if (!state.isListening && state.text.isNotBlank() && state.text != lastSpeechText) {
                    lastSpeechText = state.text
                    autoProcessJob?.cancel()
                    autoProcessJob = scope.launch {
                        delay(300)
                        processVoiceQuery(state.text)
                    }
                }
            }
        }
    }

    fun startListening() {
        voiceSynthesizer.stop()
        _results.value = emptyList()
        _aiInterpretation.value = ""
        lastSpeechText = ""
        speechRecognizer.startListening()
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }

    fun clearResults() {
        _results.value = emptyList()
        _aiInterpretation.value = ""
        lastSpeechText = ""
    }

    fun processVoiceQuery(query: String) {
        if (query.isBlank()) return
        scope.launch {
            _isLoading.value = true
            _aiInterpretation.value = "🤖 Thinking..."
            try {
                // Step 1: Parse query intent with Gemini AI
                val params = aiSearchUseCase(query)
                _latestParams.value = params

                val displayMsg = params.aiResponse
                    ?: buildFallbackMessage(params, query)
                _aiInterpretation.value = displayMsg

                // Speak the AI response
                params.aiResponse?.let { voiceSynthesizer.speak(it) }

                // Step 2: Use AI-parsed params to fetch movies/shows from TMDB
                val items = aiDiscoverUseCase(params)
                _results.value = items

                if (items.isEmpty()) {
                    _aiInterpretation.value = "Couldn't find anything matching \"$query\". Try being more specific!"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Try a basic text search as last resort
                try {
                    _aiInterpretation.value = "Searching for \"$query\"..."
                    val fallback = com.kiran.movie.domain.repository.AiSearchParameters(
                        title = query,
                        aiResponse = "Showing results for \"$query\""
                    )
                    _latestParams.value = fallback
                    val items = aiDiscoverUseCase(fallback)
                    _results.value = items
                    _aiInterpretation.value = fallback.aiResponse ?: "Showing results for \"$query\""
                } catch (e2: Exception) {
                    e2.printStackTrace()
                    _aiInterpretation.value = "No results found for \"$query\""
                }
                _isLoading.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun buildFallbackMessage(params: AiSearchParameters, original: String): String {
        return when {
            !params.title.isNullOrBlank() -> "Searching for \"${params.title}\"..."
            !params.genres.isNullOrEmpty() -> "Finding ${if (params.isTvShow) "TV shows" else "movies"} for you..."
            else -> "Searching for \"$original\"..."
        }
    }

    fun dispose() {
        voiceSynthesizer.stop()
        // Reset the recognizer so its leftover text doesn't re-trigger
        // the next ViewModel instance created for this screen.
        speechRecognizer.stopListening()
        scope.cancel()
    }
}
