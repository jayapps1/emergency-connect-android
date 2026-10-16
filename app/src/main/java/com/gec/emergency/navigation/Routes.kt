package com.gec.emergency.navigation

object Routes {
    const val HOME = "home"
    const val NEARBY = "nearby"
    const val EMERGENCY = "emergency"
    const val AGENCIES = "agencies"
    const val OFFLINE_DIRECTORY = "offline_directory"
    const val REPORTS = "reports"
    const val SILENT_REPORT = "silent_report"
    const val GENERAL_REPORT = "general_report"
    const val REPORT_DETAILS_PATTERN = "report_details/{reportId}"
    const val REPORT_DETAILS = "report_details"
    const val SAFETY = "safety"
    const val STATIONS_PATTERN = "stations/{agencyId}"
    const val STATIONS = "stations"
    const val STATION_DETAILS_PATTERN = "station_details/{stationId}"
    const val STATION_DETAILS = "station_details"
    const val SETTINGS = "settings"

    fun stationsForAgency(agencyId: String) = "stations/$agencyId"
    fun stationDetails(stationId: String) = "station_details/$stationId"
    fun reportDetails(reportId: String) = "report_details/$reportId"
}
