package com.gec.emergency.domain.model

data class AnonymousSession(
    val id: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val installationScoped: Boolean = true,
)
