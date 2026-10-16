package com.gec.emergency.domain.model

/**
 * Encapsulates parameters for a nearby emergency station search.
 */
data class NearbyStationQuery(
    val location: GeoLocation,
    val agencyId: String? = null,
    val radiusKm: Double = 10.0,
    val limit: Int = 20,
) {
    init {
        require(radiusKm > 0.0) { "Search radius must be greater than zero" }
        require(limit > 0) { "Search limit must be greater than zero" }
    }
}
