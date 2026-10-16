package com.gec.emergency.domain

import com.gec.emergency.domain.model.GeoLocation
import com.gec.emergency.domain.service.HaversineDistanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class HaversineDistanceCalculatorTest {

    private val calculator = HaversineDistanceCalculator()

    @Test
    fun distanceKm_sameLocation_returnsZero() {
        val location = GeoLocation(latitude = 5.5500, longitude = -0.2000)
        val distance = calculator.distanceKm(location, location)
        assertEquals(0.0, distance, 0.0001)
    }

    @Test
    fun distanceKm_knownCoordinates_calculatesAccurateDistance() {
        val loc1 = GeoLocation(latitude = 5.5500, longitude = -0.2000)
        val loc2 = GeoLocation(latitude = 5.5560, longitude = -0.1980)

        val distance = calculator.distanceKm(loc1, loc2)

        assertEquals(0.70, distance, 0.1)
    }
}
