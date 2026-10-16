package com.gec.emergency.data.remote.dto

data class ReportStatusHistoryDto(
    val id: String,
    val reportId: String,
    val status: String,
    val timestampMillis: Long,
    val note: String? = null,
)
