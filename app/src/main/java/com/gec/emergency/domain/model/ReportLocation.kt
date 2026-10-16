package com.gec.emergency.domain.model

data class ReportLocation(
    val geoLocation: GeoLocation,
    val landmark: String? = null,
    val locality: String? = null,
    val district: String? = null,
    val region: String? = null,
)
