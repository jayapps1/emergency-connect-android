package com.gec.emergency.data.remote.dto

data class TrackedReportResponseDto(
    val publicReference: String,
    val categoryName: String,
    val status: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val targetAgencyTypes: Set<String> = emptySet(),
    val statusHistory: List<ReportStatusHistoryDto> = emptyList(),
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locality: String? = null,
    val agencyAssignments: List<AgencyAssignmentDetailDto> = emptyList(),
) {
    data class ReportStatusHistoryDto(
        val status: String,
        val note: String? = null,
        val timestamp: String? = null,
    )

    data class AgencyAssignmentDetailDto(
        val assignmentId: String,
        val agencyType: String,
        val agencyName: String,
        val stationId: String? = null,
        val stationName: String? = null,
        val status: String,
        val assignedAt: String? = null,
        val acknowledgedAt: String? = null,
        val inProgressAt: String? = null,
        val resolvedAt: String? = null,
        val review: AssignmentReviewDto? = null,
    )

    data class AssignmentReviewDto(
        val reviewId: String,
        val rating: Int,
        val responseSpeedRating: Int? = null,
        val professionalismRating: Int? = null,
        val helpfulnessRating: Int? = null,
        val comment: String? = null,
        val createdAt: String? = null,
    )
}
