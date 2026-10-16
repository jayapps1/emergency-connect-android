package com.gec.emergency.domain

import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.model.Station
import com.gec.emergency.domain.model.StationContact
import com.gec.emergency.domain.model.StationContactType
import org.junit.Assert.assertEquals
import org.junit.Test

class StationValidationTest {

    @Test
    fun station_validCoordinates_createsSuccessfully() {
        val station = Station(
            id = "st_01",
            agencyId = "agency_gnfs",
            name = "Test Fire Station",
            code = "TFS-01",
            region = "Greater Accra",
            district = "Accra",
            latitude = 5.5500,
            longitude = -0.2000,
        )
        assertEquals(5.5500, station.latitude, 0.0001)
        assertEquals(-0.2000, station.longitude, 0.0001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun station_invalidLatitudeUpper_throwsException() {
        Station(
            id = "st_01",
            agencyId = "agency_gnfs",
            name = "Test Fire Station",
            code = "TFS-01",
            region = "Greater Accra",
            district = "Accra",
            latitude = 91.0,
            longitude = -0.2000,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun station_invalidLongitudeLower_throwsException() {
        Station(
            id = "st_01",
            agencyId = "agency_gnfs",
            name = "Test Fire Station",
            code = "TFS-01",
            region = "Greater Accra",
            district = "Accra",
            latitude = 5.5500,
            longitude = -181.0,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun stationContact_invalidPriorityZero_throwsException() {
        StationContact(
            id = "sc_01",
            stationId = "st_01",
            phoneNumber = "+233302000001",
            type = StationContactType.WATCHROOM,
            priority = 0,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun stationContact_blankPhoneNumber_throwsException() {
        StationContact(
            id = "sc_01",
            stationId = "st_01",
            phoneNumber = "   ",
            type = StationContactType.WATCHROOM,
            priority = 1,
        )
    }

    @Test
    fun geoLocation_validCoordinates_createsSuccessfully() {
        val location = GeoLocation(
            latitude = 5.5500,
            longitude = -0.2000,
            accuracyMeters = 10.0,
        )
        assertEquals(5.5500, location.latitude, 0.0001)
        assertEquals(10.0, location.accuracyMeters!!, 0.0001)
    }
}
