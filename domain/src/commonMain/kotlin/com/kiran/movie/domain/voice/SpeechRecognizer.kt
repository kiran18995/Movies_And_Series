package com.kiran.movie.domain.voice

import kotlinx.coroutines.flow.StateFlow

data class SpeechState(
    val isListening: Boolean = false,
    val text: String = "",
    val error: String? = null
)

interface SpeechRecognizer {
    val state: StateFlow<SpeechState>

    fun startListening()
    fun stopListening()
}
