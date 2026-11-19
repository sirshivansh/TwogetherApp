package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.appcompat.app.AppCompatDelegate
import com.example.R
import com.example.ui.components.localizedForCurrentLocale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

private fun activeSpeechLocale(): Locale {
    val appLocale = AppCompatDelegate.getApplicationLocales()
    val tag = if (!appLocale.isEmpty) appLocale[0]?.language else Locale.getDefault().language
    return when (val clean = tag?.lowercase(Locale.US)) {
        "as", "bn", "brx", "doi", "gu", "hi", "kn", "ks", "kok",
        "mai", "ml", "mni", "mr", "ne", "or", "pa", "sa", "sat",
        "sd", "ta", "te", "ur" -> Locale.forLanguageTag("$clean-IN")
        else -> Locale.forLanguageTag("en-IN")
    }
}

/**
 * Accessible Text-to-Speech speaker for "Listen" buttons across Twogether.
 * Automatically adapts speech locale to English, Hindi, or Marathi.
 */
class TtsSpeaker(context: Context) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _speakingId = MutableStateFlow<String?>(null)
    val speakingId: StateFlow<String?> = _speakingId.asStateFlow()

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                applyLocaleIfSupported(activeSpeechLocale())
                tts?.setSpeechRate(0.92f)
                isReady = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _speakingId.value = utteranceId
                    }

                    override fun onDone(utteranceId: String?) {
                        if (_speakingId.value == utteranceId) {
                            _speakingId.value = null
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (_speakingId.value == utteranceId) {
                            _speakingId.value = null
                        }
                    }
                })
            }
        }
    }

    private fun applyLocaleIfSupported(locale: Locale) {
        val engine = tts ?: return
        val res = engine.setLanguage(locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.language = Locale.US
        }
    }

    fun toggleSpeak(id: String, text: String) {
        if (_speakingId.value == id) {
            stop()
            return
        }
        if (!isReady || text.isBlank()) return
        applyLocaleIfSupported(activeSpeechLocale())
        _speakingId.value = id
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    fun stop() {
        tts?.stop()
        _speakingId.value = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

/**
 * Voice-First SpeechRecognizer wrapper with clean localized error/status reporting.
 */
class VoiceInputController(
    context: Context,
    private val onResultText: (String) -> Unit,
    private val onStatusMessage: (String) -> Unit
) {
    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(appContext)

    fun startListening() {
        val localized = appContext.localizedForCurrentLocale()
        if (!isAvailable()) {
            onStatusMessage(localized.getString(R.string.voice_unavailable))
            return
        }
        try {
            recognizer?.destroy()
            val sr = SpeechRecognizer.createSpeechRecognizer(appContext)
            recognizer = sr
            sr.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    onStatusMessage(appContext.localizedForCurrentLocale().getString(R.string.voice_retry))
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val best = matches?.firstOrNull().orEmpty()
                    if (best.isNotBlank()) {
                        onResultText(best)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        onResultText(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val speechLocale = activeSpeechLocale()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLocale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            _isListening.value = true
            sr.startListening(intent)
        } catch (_: Exception) {
            _isListening.value = false
            onStatusMessage(appContext.localizedForCurrentLocale().getString(R.string.voice_start_error))
        }
    }

    fun stopListening() {
        try {
            recognizer?.stopListening()
        } catch (_: Exception) {
        }
        _isListening.value = false
    }

    fun destroy() {
        try {
            recognizer?.destroy()
        } catch (_: Exception) {
        }
        recognizer = null
        _isListening.value = false
    }
}
