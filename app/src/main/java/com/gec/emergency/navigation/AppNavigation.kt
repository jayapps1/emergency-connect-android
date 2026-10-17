package com.gec.emergency.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gec.emergency.feature.emergency.EmergencyScreen
import com.gec.emergency.feature.emergency.OfflineEmergencyDirectoryRoute
import com.gec.emergency.feature.home.HomeRoute
import com.gec.emergency.feature.nearby.NearbyRoute
import com.gec.emergency.feature.reports.ReportsScreen
import com.gec.emergency.feature.reports.details.ReportDetailsRoute
import com.gec.emergency.feature.reports.general.GeneralReportRoute
import com.gec.emergency.feature.reports.silent.SilentReportRoute
import com.gec.emergency.feature.safety.SafetyScreen
import com.gec.emergency.feature.settings.SettingsRoute
import com.gec.emergency.feature.stations.AgencySelectorRoute
import com.gec.emergency.feature.stations.StationDetailsRoute
import com.gec.emergency.feature.stations.StationsRoute

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeRoute(
                onEmergencyServicesClick = {
                    navController.navigate(Routes.EMERGENCY)
                },
                onReportIncidentClick = {
                    navController.navigate(Routes.REPORTS)
                },
                onSafetyClick = {
                    navController.navigate(Routes.SAFETY)
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
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

        composable(Routes.EMERGENCY) {
            EmergencyScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onBrowseAgenciesClick = {
                    navController.navigate(Routes.AGENCIES)
                },
                onNearbyStationsClick = {
                    navController.navigate(Routes.NEARBY)
                },
                onSilentEmergencyReportClick = {
                    navController.navigate(Routes.SILENT_REPORT)
                },
            )
        }

        composable(Routes.AGENCIES) {
            AgencySelectorRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onAgencySelected = { agencyId ->
                    navController.navigate(Routes.stationsForAgency(agencyId))
                },
                onOfflineDirectoryClick = {
                    navController.navigate(Routes.OFFLINE_DIRECTORY)
                },
            )
        }

        composable(Routes.OFFLINE_DIRECTORY) {
            OfflineEmergencyDirectoryRoute(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Routes.STATIONS_PATTERN,
            arguments = listOf(
                navArgument("agencyId") { type = NavType.StringType }
            ),
        ) { backStackEntry ->
            val agencyId = backStackEntry.arguments?.getString("agencyId")
            StationsRoute(
                agencyId = agencyId,
                onBackClick = {
                    navController.popBackStack()
                },
                onStationClick = { stationId ->
                    navController.navigate(Routes.stationDetails(stationId))
                },
                onOfflineDirectoryClick = {
                    navController.navigate(Routes.OFFLINE_DIRECTORY)
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
                onOfflineDirectoryClick = {
                    navController.navigate(Routes.OFFLINE_DIRECTORY)
                },
            )
        }

        composable(Routes.NEARBY) {
            NearbyRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onBrowseAllServicesClick = {
                    navController.navigate(Routes.OFFLINE_DIRECTORY)
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

        composable(Routes.SILENT_REPORT) {
            SilentReportRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onViewReportDetails = { reportId ->
                    navController.navigate(Routes.reportDetails(reportId))
                },
                onNavigateToEmergencyCall = {
                    navController.navigate(Routes.STATIONS)
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

        composable(Routes.SAFETY) {
            SafetyScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
    }
}
