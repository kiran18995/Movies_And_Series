package com.kiran.movie.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer as AndroidSpeechRecognizerApi
import com.kiran.movie.domain.voice.SpeechRecognizer
import com.kiran.movie.domain.voice.SpeechState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidSpeechRecognizer(private val context: Context) : SpeechRecognizer {
    private val _state = MutableStateFlow(SpeechState())
    override val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var speechRecognizer: AndroidSpeechRecognizerApi? = null

    override fun startListening() {
        if (!AndroidSpeechRecognizerApi.isRecognitionAvailable(context)) {
            _state.value = _state.value.copy(error = "Speech recognition is not available")
            return
        }

        speechRecognizer = AndroidSpeechRecognizerApi.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = _state.value.copy(isListening = true, error = null, text = "")
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _state.value = _state.value.copy(isListening = false)
                }

                override fun onError(error: Int) {
                    _state.value = _state.value.copy(isListening = false, error = "Error code: $error")
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(AndroidSpeechRecognizerApi.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _state.value = _state.value.copy(text = matches[0], isListening = false)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(AndroidSpeechRecognizerApi.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _state.value = _state.value.copy(text = matches[0])
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    override fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _state.value = _state.value.copy(isListening = false)
    }
}
