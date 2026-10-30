package com.gec.emergency.navigation

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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gec.emergency.domain.model.SafetyInformation
import com.gec.emergency.feature.account.AccountScreen
import com.gec.emergency.feature.account.AccountViewModel
import com.gec.emergency.feature.account.MyReportsScreen
import com.gec.emergency.feature.auth.OtpVerifyScreen
import com.gec.emergency.feature.auth.RegisterScreen
import com.gec.emergency.feature.home.HomeRoute
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
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
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
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
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

        composable(Routes.ACCOUNT) {
            val viewModel: AccountViewModel = hiltViewModel()
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
                    navController.navigate(Routes.REGISTER)
                },
                onMyReportsClick = {
                    navController.navigate(Routes.MY_REPORTS)
                },
                onSignOutClick = {
                    viewModel.signOut()
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
                onDisableTotp = {
                    viewModel.disableTotp()
                },
                onDismissTotpDialog = {
                    viewModel.dismissTotpSetupDialog()
                },
            )
        }

        composable(Routes.REGISTER) {
            val viewModel: AccountViewModel = hiltViewModel()
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
                    viewModel.requestRegisterOtp(phone)
                    navController.navigate(Routes.OTP_VERIFY)
                },
                onContinueAsGuest = {
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.OTP_VERIFY) {
            val viewModel: AccountViewModel = hiltViewModel()
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

        composable(Routes.MY_REPORTS) {
            val viewModel: AccountViewModel = hiltViewModel()
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
