package com.gec.emergency.data.remote.dto

data class IncidentCategoryResponseDto(
    val id: String,
    val code: String,
    val name: String,
    val description: String? = null,
    val defaultSeverity: String,
    val supportsSilent: Boolean,
    val supportsGeneral: Boolean,
    val supportsCall: Boolean,
    val defaultAgencyTypes: Set<String> = emptySet(),
)
