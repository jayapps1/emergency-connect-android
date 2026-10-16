package com.gec.emergency.data.remote.dto

data class AgencyResponseDto(
    val id: String,
    val name: String,
    val code: String,
    val type: String,
    val emergencyNumber: String? = null,
    val active: Boolean = true,
    val verified: Boolean = true,
)
