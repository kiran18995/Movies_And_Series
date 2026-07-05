package com.kiran.movie.data.voice

import com.kiran.movie.domain.voice.SpeechRecognizer
import com.kiran.movie.domain.voice.SpeechState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.Foundation.NSError
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
class IosSpeechRecognizer : SpeechRecognizer {
    private val _state = MutableStateFlow(SpeechState())
    override val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var speechRecognizer: SFSpeechRecognizer? = null
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest? = null
    private var audioEngine: AVAudioEngine? = null

    override fun startListening() {
        SFSpeechRecognizer.requestAuthorization { status ->
            if (status != SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized) {
                _state.value = _state.value.copy(error = "Speech recognition not authorized")
                return@requestAuthorization
            }

            try {
                startRecording()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Failed to start recording")
            }
        }
    }

    private fun startRecording() {
        _state.value = _state.value.copy(isListening = true, text = "", error = null)

        speechRecognizer = SFSpeechRecognizer()
        audioEngine = AVAudioEngine()
        recognitionRequest = SFSpeechAudioBufferRecognitionRequest()

        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(AVAudioSessionCategoryRecord, mode = AVAudioSessionModeMeasurement, options = 0u, error = null)
        audioSession.setActive(true, error = null)

        val inputNode = audioEngine?.inputNode
        val request = recognitionRequest ?: return

        request.shouldReportPartialResults = true

        speechRecognizer?.recognitionTaskWithRequest(request) { result, error ->
            var isFinal = false
            if (result != null) {
                _state.value = _state.value.copy(text = result.bestTranscription.formattedString)
                isFinal = result.isFinal()
            }

            if (error != null || isFinal) {
                audioEngine?.stop()
                inputNode?.removeTapOnBus(0u)
                recognitionRequest = null
                _state.value = _state.value.copy(isListening = false, error = error?.localizedDescription)
            }
        }

        val recordingFormat = inputNode?.outputFormatForBus(0u)
        inputNode?.installTapOnBus(0u, 1024u, recordingFormat) { buffer, _ ->
            buffer?.let { recognitionRequest?.appendAudioPCMBuffer(it) }
        }

        audioEngine?.prepare()
        audioEngine?.startAndReturnError(null)
    }

    override fun stopListening() {
        audioEngine?.stop()
        recognitionRequest?.endAudio()
        _state.value = _state.value.copy(isListening = false)
    }
}
