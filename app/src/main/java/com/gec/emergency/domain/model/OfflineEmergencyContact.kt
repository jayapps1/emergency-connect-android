package com.gec.emergency.domain.model

data class OfflineEmergencyContact(
    val id: String,
    val agencyType: AgencyType,
    val label: String,
    val phoneNumber: String,
    val priority: Int = 1,
    val isNational: Boolean = false,
    val isActive: Boolean = true,
) {
    init {
        require(id.isNotBlank()) { "ID must not be blank" }
        require(label.isNotBlank()) { "Label must not be blank" }
        require(phoneNumber.isNotBlank()) { "Phone number must not be blank" }
        require(priority >= 1) { "Priority must be 1 or higher" }
    }
}
