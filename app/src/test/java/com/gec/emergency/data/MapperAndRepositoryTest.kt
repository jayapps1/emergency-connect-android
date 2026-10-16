package com.gec.emergency.data

import com.gec.emergency.data.mapper.toDomainModel
import com.gec.emergency.data.remote.dto.AgencyResponseDto
import com.gec.emergency.data.remote.dto.StationContactResponseDto
import com.gec.emergency.data.remote.dto.StationResponseDto
import com.gec.emergency.domain.model.AgencyType
import com.gec.emergency.domain.model.StationContactType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapperAndRepositoryTest {

    @Test
    fun agencyResponseDto_mapsToDomainAgency() {
        val dto = AgencyResponseDto(
            id = "gnfs",
            name = "Ghana National Fire Service",
            code = "GNFS",
            type = "FIRE",
            emergencyNumber = "192",
            active = true,
            verified = true,
        )

        val domain = dto.toDomainModel()

        assertEquals("gnfs", domain.id)
        assertEquals("GNFS", domain.code)
        assertEquals(AgencyType.FIRE, domain.type)
        assertTrue(domain.isActive)
        assertTrue(domain.isVerified)
    }

    @Test
    fun stationResponseDto_mapsToDomainStation() {
        val dto = StationResponseDto(
            id = "st_01",
            agencyId = "gnfs",
            name = "GNFS HQ Station",
            code = "GNFS-ACC-01",
            region = "Greater Accra",
            district = "Accra",
            locality = "Makola",
            latitude = 5.5500,
            longitude = -0.2000,
            locationAccuracyMeters = 10.0,
            active = true,
            verified = true,
        )

        val domain = dto.toDomainModel()

        assertEquals("st_01", domain.id)
        assertEquals("gnfs", domain.agencyId)
        assertEquals(5.5500, domain.latitude, 0.0001)
        assertEquals(-0.2000, domain.longitude, 0.0001)
    }

    @Test
    fun stationContactResponseDto_mapsToDomainStationContact() {
        val dto = StationContactResponseDto(
            id = "sc_01",
            stationId = "st_01",
            phoneNumber = "+233302000001",
            type = "WATCHROOM",
            priority = 1,
            is24Hour = true,
            active = true,
            verified = true,
        )

        val domain = dto.toDomainModel()

        assertEquals("sc_01", domain.id)
        assertEquals("st_01", domain.stationId)
        assertEquals("+233302000001", domain.phoneNumber)
        assertEquals(StationContactType.WATCHROOM, domain.type)
        assertEquals(1, domain.priority)
    }

    @Test
    fun unknownAgencyType_mapsToOther() {
        val dto = AgencyResponseDto(
            id = "agency_unknown",
            name = "New Agency",
            code = "NEW",
            type = "UNKNOWN_FUTURE_TYPE",
            active = true,
            verified = true,
        )

        val domain = dto.toDomainModel()

        assertEquals(AgencyType.OTHER, domain.type)
    }

    @Test
    fun unknownStationContactType_mapsToOther() {
        val dto = StationContactResponseDto(
            id = "sc_unknown",
            stationId = "st_01",
            phoneNumber = "0000",
            type = "UNKNOWN_CONTACT_TYPE",
            priority = 1,
            active = true,
            verified = true,
        )

        val domain = dto.toDomainModel()

        assertEquals(StationContactType.OTHER, domain.type)
    }
}
