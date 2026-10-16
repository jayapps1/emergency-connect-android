package com.gec.emergency.data.remote.dto

data class StationContactResponseDto(
    val id: String,
    val stationId: String,
    val phoneNumber: String,
    val type: String,
    val priority: Int = 1,
    val is24Hour: Boolean = true,
    val active: Boolean = true,
    val verified: Boolean = true,
)
