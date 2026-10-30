package com.gec.emergency.core.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
import com.gec.emergency.domain.model.AppLanguage
import java.util.Locale

private const val TAG = "GEC_LANGUAGE_TRACE"

/**
 * Application locale is changed only when the requested locale differs
 * from the currently active locale; repeated application would recreate
 * the Activity continuously.
 */
object LocaleHelper {

    fun updateLocale(context: Context, language: AppLanguage): Context {
        val targetTag = language.code

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                val managerLocales = localeManager?.applicationLocales
                val activeTag = if (managerLocales != null && !managerLocales.isEmpty) {
                    managerLocales.get(0).toLanguageTag()
                } else {
                    ""
                }

                val isAlreadyActive = when (targetTag.lowercase(Locale.US)) {
                    "tw" -> activeTag.equals("tw", ignoreCase = true) || activeTag.equals("ak", ignoreCase = true)
                    "gaa" -> activeTag.equals("gaa", ignoreCase = true) || activeTag.equals("ga", ignoreCase = true)
                    else -> activeTag.equals(targetTag, ignoreCase = true)
                }

                if (isAlreadyActive) {
                    Log.d(TAG, "APPLY_EXECUTED: activeTag=$activeTag requested=$targetTag applying=false (already active)")
                    return context
                }

                Log.d(TAG, "APPLY_EXECUTED: activeTag=$activeTag requested=$targetTag applying=true")
                localeManager?.applicationLocales = LocaleList.forLanguageTags(targetTag)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update LocaleManager applicationLocales", e)
            }
        }

        val locale = Locale.forLanguageTag(targetTag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
