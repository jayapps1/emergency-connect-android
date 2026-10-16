package com.gec.emergency.core.util

import android.content.Context
import android.content.res.Configuration
import com.gec.emergency.domain.model.AppLanguage
import java.util.Locale

object LocaleHelper {

    fun updateLocale(context: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.code)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
