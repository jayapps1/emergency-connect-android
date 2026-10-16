package com.gec.emergency.domain.model

data class ReportStatusHistory(
    val id: String,
    val reportId: String,
    val status: ReportStatus,
    val timestampMillis: Long = System.currentTimeMillis(),
    val note: String? = null,
)
