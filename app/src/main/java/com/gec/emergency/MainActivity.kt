package com.gec.emergency

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gec.emergency.core.util.LocaleHelper
import com.gec.emergency.domain.model.AppLanguage
import com.gec.emergency.domain.model.AppThemeMode
import com.gec.emergency.domain.usecase.ObserveAppLanguageUseCase
import com.gec.emergency.domain.usecase.ObserveLargeTextUseCase
import com.gec.emergency.domain.usecase.ObserveThemeModeUseCase
import com.gec.emergency.navigation.AppNavigation
import com.gec.emergency.ui.theme.GhanaEmergencyConnectTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var observeThemeModeUseCase: ObserveThemeModeUseCase

    @Inject
    lateinit var observeLargeTextUseCase: ObserveLargeTextUseCase

    @Inject
    lateinit var observeAppLanguageUseCase: ObserveAppLanguageUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by observeThemeModeUseCase().collectAsStateWithLifecycle(initialValue = AppThemeMode.SYSTEM)
            val largeText by observeLargeTextUseCase().collectAsStateWithLifecycle(initialValue = false)
            val language by observeAppLanguageUseCase().collectAsStateWithLifecycle(initialValue = AppLanguage.ENGLISH)

            LaunchedEffect(language) {
                LocaleHelper.updateLocale(this@MainActivity, language)
            }

            GhanaEmergencyConnectTheme(
                themeMode = themeMode,
                largeText = largeText,
            ) {
                AppNavigation()
            }
        }
    }
}
