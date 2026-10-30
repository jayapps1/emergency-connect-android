package com.gec.emergency.data.local

import com.gec.emergency.domain.model.Agency
import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.model.StationContactType

/**
 * TEST FIXTURE ONLY.
 * In-memory emergency data source for unit testing.
 */
class DevelopmentEmergencyDataSource {

    private val agencies = listOf(
        Agency(
            id = "agency_gnfs",
            name = "Ghana National Fire Service",
            code = "GNFS",
            type = AgencyType.FIRE,
            emergencyNumber = "192",
            isActive = true,
            isVerified = true,
        ),
        Agency(
            id = "agency_gps",
            name = "Ghana Police Service",
            code = "GPS",
            type = AgencyType.POLICE,
            emergencyNumber = "191",
            isActive = true,
            isVerified = true,
        ),
        Agency(
            id = "agency_nas",
            name = "National Ambulance Service",
            code = "NAS",
            type = AgencyType.AMBULANCE,
            emergencyNumber = "193",
            isActive = true,
            isVerified = true,
        ),
    )

    private val stations = listOf(
        Station(
            id = "st_gnfs_accra",
            agencyId = "agency_gnfs",
            name = "GNFS Headquarters Station",
            code = "GNFS-ACC-01",
            region = "Greater Accra",
            district = "Accra Metropolitan",
            locality = "Makola",
            latitude = 5.5500,
            longitude = -0.2000,
            locationAccuracyMeters = 10.0,
            isActive = true,
            isVerified = true,
        ),
        Station(
            id = "st_gps_accra",
            agencyId = "agency_gps",
            name = "Accra Central Police Station",
            code = "GPS-ACC-01",
            region = "Greater Accra",
            district = "Accra Metropolitan",
            locality = "Kinbu",
            latitude = 5.5560,
            longitude = -0.1980,
            locationAccuracyMeters = 10.0,
            isActive = true,
            isVerified = true,
        ),
        Station(
            id = "st_nas_accra",
            agencyId = "agency_nas",
            name = "Accra Regional Ambulance Station",
            code = "NAS-ACC-01",
            region = "Greater Accra",
            district = "Accra Metropolitan",
            locality = "Ridge",
            latitude = 5.5600,
            longitude = -0.1900,
            locationAccuracyMeters = 10.0,
            isActive = true,
            isVerified = true,
        ),
    )

    private val contacts = listOf(
        StationContact(
            id = "sc_gnfs_01",
            stationId = "st_gnfs_accra",
            phoneNumber = "+233-00-000-0001",
            type = StationContactType.WATCHROOM,
            priority = 1,
            isPublic = true,
            is24Hour = true,
            isActive = true,
            isVerified = true,
        ),
        StationContact(
            id = "sc_gnfs_02",
            stationId = "st_gnfs_accra",
            phoneNumber = "+233-00-000-0002",
            type = StationContactType.CONTROL_ROOM,
            priority = 2,
            isPublic = true,
            is24Hour = true,
            isActive = true,
            isVerified = true,
        ),
        StationContact(
            id = "sc_gps_01",
            stationId = "st_gps_accra",
            phoneNumber = "+233-00-000-0003",
            type = StationContactType.CONTROL_ROOM,
            priority = 1,
            isPublic = true,
            is24Hour = true,
            isActive = true,
            isVerified = true,
        ),
    )

    fun getAgencies(): List<Agency> = agencies
    fun getAgencyById(id: String): Agency? = agencies.find { it.id == id }
    fun getStations(): List<Station> = stations
    fun getStationsByAgency(agencyId: String): List<Station> = stations.filter { it.agencyId == agencyId }
    fun getStationById(id: String): Station? = stations.find { it.id == id }
    fun getContactsForStation(stationId: String): List<StationContact> = contacts.filter { it.stationId == stationId }.sortedBy { it.priority }
}
