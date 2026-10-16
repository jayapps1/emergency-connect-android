package com.gec.emergency.domain.model

/**
 * Represents a contact number for an emergency station.
 *
 * Convention: [priority] must be >= 1. Priority 1 represents the primary/preferred contact number.
 */
data class StationContact(
    val id: String,
    val stationId: String,
    val phoneNumber: String,
    val type: StationContactType,
    val priority: Int = 1,
    val isPublic: Boolean = true,
    val is24Hour: Boolean = true,
    val isActive: Boolean = true,
    val isVerified: Boolean = true,
) {
    init {
        require(id.isNotBlank()) { "StationContact ID must not be blank" }
        require(stationId.isNotBlank()) { "StationId must not be blank" }
        require(phoneNumber.isNotBlank()) { "Phone number must not be blank" }
        require(priority >= 1) { "Priority must be 1 or higher (where 1 is primary/preferred)" }
    }
}
