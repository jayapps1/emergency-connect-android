package com.gec.emergency.domain.service

import com.gec.emergency.domain.model.GeoLocation

/**
 * Service contract for calculating geographic distance between two locations.
 */
interface DistanceCalculator {
    fun distanceKm(from: GeoLocation, to: GeoLocation): Double
}
