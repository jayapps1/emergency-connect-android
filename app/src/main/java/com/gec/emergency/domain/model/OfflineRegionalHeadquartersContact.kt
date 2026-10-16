package com.gec.emergency.domain.model

data class OfflineRegionalHeadquartersContact(
    val id: String,
    val agencyId: String,
    val agencyCode: String,
    val agencyName: String,
    val agencyType: AgencyType,
    val regionId: String? = null,
    val regionName: String,
    val stationId: String,
    val stationName: String,
    val stationLevel: String = "RHQ",
    val phoneNumber: String,
    val contactType: String = "CONTROL_ROOM",
    val priority: Int = 1,
    val isAvailable24Hour: Boolean = true,
    val lastSuccessfulSyncAt: Long = System.currentTimeMillis(),
) {
    init {
        require(id.isNotBlank()) { "ID must not be blank" }
        require(agencyId.isNotBlank()) { "Agency ID must not be blank" }
        require(phoneNumber.isNotBlank()) { "Phone number must not be blank" }
    }
}
