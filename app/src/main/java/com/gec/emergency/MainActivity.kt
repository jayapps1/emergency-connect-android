package com.gec.emergency

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gec.emergency.domain.model.AppThemeMode
import com.gec.emergency.domain.usecase.ObserveLargeTextUseCase
import com.gec.emergency.domain.usecase.ObserveThemeModeUseCase
import com.gec.emergency.navigation.AppNavigation
import com.gec.emergency.ui.theme.GhanaEmergencyConnectTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "GEC_LANGUAGE_TRACE"

/**
 * Language preference persistence is independent from Activity
 * recreation and authentication state.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var observeThemeModeUseCase: ObserveThemeModeUseCase

    @Inject
    lateinit var observeLargeTextUseCase: ObserveLargeTextUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d(TAG, "ACTIVITY_CREATE: instance=${hashCode()}")

        setContent {
            val themeMode by observeThemeModeUseCase().collectAsStateWithLifecycle(initialValue = AppThemeMode.SYSTEM)
            val largeText by observeLargeTextUseCase().collectAsStateWithLifecycle(initialValue = false)

            GhanaEmergencyConnectTheme(
                themeMode = themeMode,
                largeText = largeText,
            ) {
                AppNavigation()
            }
        }
    }
}
