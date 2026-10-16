package com.gec.emergency.data.remote.dto

data class TrackedReportResponseDto(
    val publicReference: String,
    val categoryName: String,
    val status: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val targetAgencyTypes: Set<String> = emptySet(),
    val statusHistory: List<ReportStatusHistoryDto> = emptyList(),
) {
    data class ReportStatusHistoryDto(
        val status: String,
        val note: String? = null,
        val timestamp: String? = null,
    )
}
