package com.gec.emergency.core.accessibility

interface VoiceGuidanceManager {
    fun speak(text: String, languageCode: String = "en")
    fun stop()
    fun isAvailable(): Boolean
}
