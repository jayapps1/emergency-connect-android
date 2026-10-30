package com.gec.emergency.data.remote.dto

/**
 * Uses a concrete request DTO because Retrofit request bodies must not
 * contain wildcard generic parameter types.
 */
data class SubmitResponseReviewRequestDto(
    val rating: Int,
    val responseSpeedRating: Int? = null,
    val professionalismRating: Int? = null,
    val helpfulnessRating: Int? = null,
    val comment: String? = null,
)
