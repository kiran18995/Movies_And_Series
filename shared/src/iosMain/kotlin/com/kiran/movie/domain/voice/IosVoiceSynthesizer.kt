package com.kiran.movie.domain.voice

import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance

class IosVoiceSynthesizer : VoiceSynthesizer {
    private val synthesizer = AVSpeechSynthesizer()

    override fun speak(text: String) {
        val utterance = AVSpeechUtterance(string = text).apply {
            voice = AVSpeechSynthesisVoice.voiceWithLanguage("en-US")
        }
        synthesizer.speakUtterance(utterance)
    }

    override fun stop() {
        if (synthesizer.isSpeaking()) {
            synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        }
    }
}
