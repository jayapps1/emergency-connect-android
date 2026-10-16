package com.gec.emergency.core.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidVoiceGuidanceManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : VoiceGuidanceManager, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
        } else {
            isInitialized = false
        }
    }

    override fun speak(text: String, languageCode: String) {
        if (!isInitialized || text.isBlank()) return

        val locale = Locale.forLanguageTag(languageCode)
        val result = tts?.setLanguage(locale)

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale.ENGLISH)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "gec_tts_id")
    }

    override fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    override fun isAvailable(): Boolean = isInitialized
}
