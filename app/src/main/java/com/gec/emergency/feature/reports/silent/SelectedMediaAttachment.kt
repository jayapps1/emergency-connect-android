package com.gec.emergency.feature.reports.silent

import android.net.Uri

data class SelectedMediaAttachment(
    val uri: Uri,
    val mediaType: String, // "IMAGE" or "VIDEO"
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
)
