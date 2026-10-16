package com.gec.emergency.domain.model

data class Agency(
    val id: String,
    val name: String,
    val code: String,
    val type: AgencyType,
    val emergencyNumber: String? = null,
    val isActive: Boolean = true,
    val isVerified: Boolean = true,
)
