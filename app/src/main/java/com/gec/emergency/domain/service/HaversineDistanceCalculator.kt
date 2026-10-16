package com.gec.emergency.domain.service

import com.gec.emergency.domain.model.GeoLocation
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calculates straight-line geographic distance in kilometers using the Haversine formula.
 * Earth radius = ~6371.0 km.
 */
@Singleton
class HaversineDistanceCalculator @Inject constructor() : DistanceCalculator {

    override fun distanceKm(from: GeoLocation, to: GeoLocation): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(from.latitude)) * Math.cos(Math.toRadians(to.latitude)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusKm * c
    }
}
