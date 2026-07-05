package com.kiran.movie.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import com.kiran.movie.domain.voice.VoiceSynthesizer
import java.util.Locale

class AndroidVoiceSynthesizer(context: Context) : VoiceSynthesizer, TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isInitialized = true
            pendingText?.let {
                speak(it)
                pendingText = null
            }
        }
    }

    override fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AiResponse")
        } else {
            pendingText = text
        }
    }

    override fun stop() {
        tts?.stop()
    }
}
