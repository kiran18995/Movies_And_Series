package com.kiran.movie.domain.voice

interface VoiceSynthesizer {
    fun speak(text: String)
    fun stop()
}
