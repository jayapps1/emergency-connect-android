package com.gec.emergency.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gec.emergency.domain.model.SafetyInformation
import com.gec.emergency.feature.account.AccountScreen
import com.gec.emergency.feature.account.AccountViewModel
import com.gec.emergency.feature.account.MyReportsScreen
import com.gec.emergency.feature.auth.ForgotPasswordScreen
import com.gec.emergency.feature.auth.LoginScreen
import com.gec.emergency.feature.auth.OtpVerifyScreen
import com.gec.emergency.feature.auth.RegisterScreen
import com.gec.emergency.feature.emergency.OfflineEmergencyDirectoryRoute
import com.gec.emergency.feature.home.HomeRoute
import com.gec.emergency.feature.home.HomeViewModel
import com.gec.emergency.feature.nearby.NearbyRoute
import com.gec.emergency.feature.notification.NotificationsScreen
import com.gec.emergency.feature.notification.NotificationsViewModel
import com.gec.emergency.feature.reports.ReportsScreen
import com.gec.emergency.feature.reports.details.ReportDetailsRoute
import com.gec.emergency.feature.reports.general.GeneralReportRoute
import com.gec.emergency.feature.reports.silent.SilentReportRoute
import com.gec.emergency.feature.safety.SafetyDetailScreen
import com.gec.emergency.feature.safety.SafetyInformationScreen
import com.gec.emergency.feature.safety.SafetyViewModel
import com.gec.emergency.feature.settings.SettingsRoute
import com.gec.emergency.feature.stations.StationDetailsRoute
import com.gec.emergency.feature.stations.StationsRoute
import com.gec.emergency.ui.components.LiquidGlassBottomNavigation

/**
 * Coordinates public-account navigation independently from staff
 * authentication. Registration, sign-in, and incident-history destinations
 * remain scoped to the citizen application.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    startDestination: String = Routes.HOME,
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val fullScreenAuthRoutes = remember {
        setOf(Routes.REGISTER, Routes.LOGIN, Routes.FORGOT_PASSWORD, Routes.OTP_VERIFY)
    }
    val showBottomBar = currentRoute != null && currentRoute !in fullScreenAuthRoutes

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                LiquidGlassBottomNavigation(
                    currentRoute = currentRoute,
                    unreadNotificationCount = homeUiState.unreadNotificationCount,
                    onNavItemSelect = { item ->
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(Routes.HOME) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(180)) + slideInHorizontally(
                    animationSpec = tween(180),
                    initialOffsetX = { it / 6 }
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(150)) + slideOutHorizontally(
                    animationSpec = tween(150),
                    targetOffsetX = { -it / 6 }
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(180)) + slideInHorizontally(
                    animationSpec = tween(180),
                    initialOffsetX = { -it / 6 }
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(150)) + slideOutHorizontally(
                    animationSpec = tween(150),
                    targetOffsetX = { it / 6 }
                )
            }
        ) {
            composable(Routes.HOME) {
                HomeRoute(
                    onEmergencyServicesClick = {
                        navController.navigate(Routes.NEARBY)
                    },
                    onReportIncidentClick = {
                        navController.navigate(Routes.REPORTS)
                    },
                    onSilentReportClick = {
                        navController.navigate(Routes.SILENT_REPORT)
                    },
                    onSafetyClick = {
                        navController.navigate(Routes.SAFETY)
                    },
                    onOfflineDirectoryClick = {
                        navController.navigate(Routes.OFFLINE_DIRECTORY)
                    },
                    onSettingsClick = {
                        navController.navigate(Routes.SETTINGS) {
                            launchSingleTop = true
                        }
                    },
                    onAccountClick = {
                        navController.navigate(Routes.ACCOUNT) {
                            launchSingleTop = true
                        }
                    },
                    onNotificationsClick = {
                        navController.navigate(Routes.NOTIFICATIONS) {
                            launchSingleTop = true
                        }
                    },
                    viewModel = homeViewModel,
                )
            }

            composable(Routes.OFFLINE_DIRECTORY) {
                OfflineEmergencyDirectoryRoute(
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }

            composable(Routes.NEARBY) {
                NearbyRoute(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onBrowseAllServicesClick = {
                        navController.navigate(Routes.STATIONS)
                    },
                    onStationClick = { stationId ->
                        navController.navigate(Routes.stationDetails(stationId))
                    },
                )
            }

            composable(Routes.STATIONS) {
                StationsRoute(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onStationClick = { stationId ->
                        navController.navigate(Routes.stationDetails(stationId))
                    },
                )
            }

            composable(
                route = Routes.STATION_DETAILS_PATTERN,
                arguments = listOf(
                    navArgument("stationId") { type = NavType.StringType }
                ),
            ) { backStackEntry ->
                val stationId = backStackEntry.arguments?.getString("stationId") ?: ""
                StationDetailsRoute(
                    stationId = stationId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }

            composable(Routes.SETTINGS) {
                SettingsRoute(
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }

            composable(Routes.ACCOUNT) { backStackEntry ->
                val viewModel: AccountViewModel = hiltViewModel(backStackEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                AccountScreen(
                    uiState = uiState,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRegisterClick = {
                        navController.navigate(Routes.REGISTER)
                    },
                    onLoginClick = {
                        navController.navigate(Routes.LOGIN)
                    },
                    onMyReportsClick = {
                        navController.navigate(Routes.MY_REPORTS)
                    },
                    onSignOutClick = {
                        viewModel.signOut()
                    },
                    onCheckAuthStatus = {
                        viewModel.checkAuthStatus()
                    },
                    onClaimReportSubmit = { ref, token ->
                        viewModel.claimReport(ref, token)
                    },
                    onStartTotpSetup = {
                        viewModel.startTotpSetup()
                    },
                    onConfirmTotpSetup = { code ->
                        viewModel.confirmTotpSetup(code)
                    },
                    onStartTotpReconfiguration = { password, onSuccess, onError ->
                        viewModel.startTotpReconfiguration(password, onSuccess, onError)
                    },
                    onConfirmTotpReconfiguration = { newSecret, code, onSuccess, onError ->
                        viewModel.confirmTotpReconfiguration(newSecret, code, onSuccess, onError)
                    },
                    onDisableTotp = {
                        viewModel.disableTotp()
                    },
                    onDismissTotpDialog = {
                        viewModel.dismissTotpSetupDialog()
                    },
                    onUpdateProfile = { fn, ln, onSuccess ->
                        viewModel.updateProfile(fn, ln, onSuccess)
                    },
                    onChangePassword = { currentPass, newPass, onSuccess, onError ->
                        viewModel.changePassword(currentPass, newPass, onSuccess, onError)
                    },
                    onGenerateRecoveryCodes = { password, onSuccess, onError ->
                        viewModel.generateRecoveryCodes(password, onSuccess, onError)
                    },
                    onSubmitPublicMfaRecoveryRequest = { reason, onSuccess, onError ->
                        viewModel.submitPublicMfaRecoveryRequest(reason, onSuccess, onError)
                    },
                    onCollectPublicRecoveryCodes = { requestId, onSuccess, onError ->
                        viewModel.collectPublicRecoveryCodes(requestId, onSuccess, onError)
                    },
                    onSubmitPhoneChangeRequest = { phone, reason, onSuccess, onError ->
                        viewModel.submitPhoneChangeRequest(phone, reason, onSuccess, onError)
                    },
                    onLoadMyRequests = {
                        viewModel.loadMyRequests()
                    },
                )
            }

            composable(Routes.LOGIN) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    runCatching { navController.getBackStackEntry(Routes.ACCOUNT) }.getOrNull() ?: backStackEntry
                }
                val viewModel: AccountViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LoginScreen(
                    uiState = uiState,
                    onPhoneNumberChanged = { phone ->
                        viewModel.onPhoneNumberInputChanged(phone)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPasswordLogin = { phone, password ->
                        viewModel.loginWithPassword(phone, password, onSuccess = {
                            navController.popBackStack(Routes.ACCOUNT, false)
                        }, onError = {})
                    },
                    onTotpLogin = { phone, totpCode ->
                        viewModel.loginWithTotp(phone, totpCode, onSuccess = {
                            navController.popBackStack(Routes.ACCOUNT, false)
                        }, onError = {})
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    },
                    onContinueAsGuest = {
                        navController.popBackStack()
                    },
                    onSubmitPublicMfaRecoveryRequest = { reason, onSuccess, onError ->
                        viewModel.submitPublicMfaRecoveryRequest(reason, onSuccess, onError)
                    },
                )
            }

            composable(Routes.FORGOT_PASSWORD) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    runCatching { navController.getBackStackEntry(Routes.ACCOUNT) }.getOrNull() ?: backStackEntry
                }
                val viewModel: AccountViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                ForgotPasswordScreen(
                    uiState = uiState,
                    onPhoneNumberChanged = { phone ->
                        viewModel.onPhoneNumberInputChanged(phone)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRequestResetOtp = { phone, onSuccess, onError ->
                        viewModel.requestForgotPasswordOtp(phone, onSuccess, onError)
                    },
                    onVerifyResetOtp = { phone, code, onSuccess, onError ->
                        viewModel.verifyForgotPasswordOtp(phone, code, onSuccess, onError)
                    },
                    onResetPassword = { phone, recoveryToken, newPass, onSuccess, onError ->
                        viewModel.resetPasswordWithToken(phone, recoveryToken, newPass, onSuccess, onError)
                    },
                    onNavigateToLogin = {
                        navController.popBackStack(Routes.LOGIN, false)
                    },
                )
            }

            composable(Routes.REGISTER) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    runCatching { navController.getBackStackEntry(Routes.ACCOUNT) }.getOrNull() ?: backStackEntry
                }
                val viewModel: AccountViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                RegisterScreen(
                    uiState = uiState,
                    onPhoneNumberChanged = { phone ->
                        viewModel.onPhoneNumberInputChanged(phone)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRequestOtp = { phone ->
                        viewModel.requestRegisterOtp(phone, onSuccess = {
                            navController.navigate(Routes.OTP_VERIFY)
                        })
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    },
                    onContinueAsGuest = {
                        navController.popBackStack()
                    },
                )
            }

            composable(Routes.OTP_VERIFY) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    runCatching { navController.getBackStackEntry(Routes.ACCOUNT) }.getOrNull() ?: backStackEntry
                }
                val viewModel: AccountViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                OtpVerifyScreen(
                    uiState = uiState,
                    phoneNumber = uiState.phoneNumberInput,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onVerifyOtp = { code ->
                        viewModel.verifyRegisterOtp(uiState.phoneNumberInput, code)
                        navController.popBackStack(Routes.ACCOUNT, false)
                    },
                    onResendOtp = {
                        viewModel.requestRegisterOtp(uiState.phoneNumberInput)
                    },
                    onRequestSmsFallback = {
                        viewModel.requestSmsFallbackOtp(uiState.phoneNumberInput)
                    },
                )
            }

            composable(Routes.MY_REPORTS) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    runCatching { navController.getBackStackEntry(Routes.ACCOUNT) }.getOrNull() ?: backStackEntry
                }
                val viewModel: AccountViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                MyReportsScreen(
                    uiState = uiState,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onReportClick = { reportId ->
                        navController.navigate(Routes.reportDetails(reportId))
                    },
                )
            }

            composable(Routes.SAFETY) {
                val viewModel: SafetyViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var selectedPostForDetail by remember { mutableStateOf<SafetyInformation?>(null) }

                if (selectedPostForDetail != null) {
                    SafetyDetailScreen(
                        post = selectedPostForDetail!!,
                        onBackClick = { selectedPostForDetail = null },
                    )
                } else {
                    SafetyInformationScreen(
                        safetyPosts = uiState.safetyPosts,
                        onBackClick = { navController.popBackStack() },
                        onPostClick = { post -> selectedPostForDetail = post },
                    )
                }
            }

            composable(Routes.REPORTS) {
                ReportsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onGeneralReportClick = {
                        navController.navigate(Routes.GENERAL_REPORT)
                    },
                )
            }

            composable(Routes.GENERAL_REPORT) {
                GeneralReportRoute(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onViewReportDetails = { reportId ->
                        navController.navigate(Routes.reportDetails(reportId))
                    },
                )
            }

            composable(Routes.SILENT_REPORT) {
                SilentReportRoute(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onViewReportDetails = { reportId ->
                        navController.navigate(Routes.reportDetails(reportId))
                    },
                    onNavigateToEmergencyCall = {
                        navController.navigate(Routes.HOME)
                    },
                )
            }

            composable(
                route = Routes.REPORT_DETAILS_PATTERN,
                arguments = listOf(
                    navArgument("reportId") { type = NavType.StringType }
                ),
            ) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
                ReportDetailsRoute(
                    reportId = reportId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }

            composable(Routes.NOTIFICATIONS) {
                val viewModel: NotificationsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                NotificationsScreen(
                    uiState = uiState,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNotificationClick = { reportId ->
                        if (!reportId.isNullOrBlank()) {
                            navController.navigate(Routes.reportDetails(reportId))
                        }
                    },
                )
            }
        }
    }
}
