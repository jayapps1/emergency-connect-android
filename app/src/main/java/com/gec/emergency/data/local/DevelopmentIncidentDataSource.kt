package com.gec.emergency.data.local

import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.IncidentCategory
import com.gec.emergency.domain.model.IncidentSeverity
import com.gec.emergency.domain.model.ReportMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DEVELOPMENT INCIDENT DATA SOURCE.
 *
 * Temporary in-memory source providing sample incident categories and development reports.
 * NOT FOR PRODUCTION DISPATCH.
 */
@Singleton
class DevelopmentIncidentDataSource @Inject constructor() {

    private val categories = listOf(
        IncidentCategory(
            id = "cat_fire",
            name = "Fire & Explosion",
            description = "Structural fires, wildfire, chemical or gas leak hazards",
            severity = IncidentSeverity.HIGH,
            reportModeSupported = setOf(ReportMode.SILENT, ReportMode.GENERAL),
            defaultAgencyTypes = setOf(AgencyType.FIRE),
        ),
        IncidentCategory(
            id = "cat_robbery",
            name = "Armed Robbery / Assault",
            description = "Active violent crime or imminent physical threat",
            severity = IncidentSeverity.CRITICAL,
            reportModeSupported = setOf(ReportMode.SILENT, ReportMode.GENERAL),
            defaultAgencyTypes = setOf(AgencyType.POLICE),
        ),
        IncidentCategory(
            id = "cat_medical",
            name = "Medical Emergency",
            description = "Severe injury, collapse, cardiac or respiratory distress",
            severity = IncidentSeverity.HIGH,
            reportModeSupported = setOf(ReportMode.SILENT, ReportMode.GENERAL),
            defaultAgencyTypes = setOf(AgencyType.AMBULANCE),
        ),
        IncidentCategory(
            id = "cat_accident",
            name = "Road Traffic Accident",
            description = "Vehicle collisions with injuries or blocked roadway",
            severity = IncidentSeverity.HIGH,
            reportModeSupported = setOf(ReportMode.SILENT, ReportMode.GENERAL),
            defaultAgencyTypes = setOf(AgencyType.POLICE, AgencyType.AMBULANCE, AgencyType.FIRE),
        ),
        IncidentCategory(
            id = "cat_suspicious",
            name = "Suspicious Activity",
            description = "Unattended items, suspicious persons, or potential threats",
            severity = IncidentSeverity.MEDIUM,
            reportModeSupported = setOf(ReportMode.SILENT, ReportMode.GENERAL),
            defaultAgencyTypes = setOf(AgencyType.POLICE),
        ),
        IncidentCategory(
            id = "cat_hazard",
            name = "Public Safety Hazard",
            description = "Flooding, fallen power lines, or structural collapse",
            severity = IncidentSeverity.MEDIUM,
            reportModeSupported = setOf(ReportMode.SILENT, ReportMode.GENERAL),
            defaultAgencyTypes = setOf(AgencyType.DISASTER_MANAGEMENT, AgencyType.FIRE),
        ),
    )

    fun getCategories(): List<IncidentCategory> = categories

    fun getCategoryById(id: String): IncidentCategory? = categories.find { it.id == id }
}
