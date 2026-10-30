package com.gec.emergency.domain.model

enum class FallbackType {
    LOCAL_STATION,
    REGIONAL_HEADQUARTERS,
    AGENCY_SHORT_CODE,
    NATIONAL_112,
}

enum class FallbackSource {
    LIVE,
    CACHE,
    BUILT_IN_SHORT_CODE,
}

/**
 * Represents a resolved emergency contact from the 4-tier fallback chain:
 * Local Station -> Regional Headquarters -> Agency Short Code -> 112
 */
data class EmergencyContactFallback(
    val type: FallbackType,
    val phoneNumber: String,
    val displayName: String,
    val source: FallbackSource,
    val stationName: String? = null,
    val regionName: String? = null,
    val is24Hour: Boolean = true,
)
