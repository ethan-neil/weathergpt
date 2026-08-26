package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAssistantManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
            Log.d("VoiceAssistantManager", "TTS successfully initialized")
        } else {
            Log.w("VoiceAssistantManager", "TTS initialization failed status: $status")
        }
    }

    fun speak(text: String, language: AppLanguage) {
        if (!isInitialized || tts == null) return

        // Configure language locale
        val locale = when (language) {
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.BENGALI -> Locale("bn", "IN")
            AppLanguage.TELUGU -> Locale("te", "IN")
            AppLanguage.TAMIL -> Locale("ta", "IN")
            AppLanguage.MARATHI -> Locale("mr", "IN")
            AppLanguage.GUJARATI -> Locale("gu", "IN")
            AppLanguage.KANNADA -> Locale("kn", "IN")
            AppLanguage.PUNJABI -> Locale("pa", "IN")
            else -> Locale("en", "US")
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.language = Locale.ENGLISH
        }

        // Clean out markdown symbols for clean natural audio read
        val cleanedText = text
            .replace("*", "")
            .replace("#", "")
            .replace("•", "")
            .replace("⚠️", "Warning:")
            .replace("🌧️", "")
            .replace("🌾", "")
            .replace("🚜", "")
            .replace("☀️", "")

        tts?.stop()
        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, "weathergpt_tts_utterance")
        _isSpeaking.value = true
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
