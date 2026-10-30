package com.gec.emergency.data.remote.dto

data class SafetyInformationResponseDto(
    val id: String,
    val sourceType: String,
    val agencyId: String? = null,
    val agencyName: String,
    val agencyCode: String,
    val title: String,
    val content: String,
    val category: String,
    val severity: String,
    val publishedAt: String,
)
