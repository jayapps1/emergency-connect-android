package com.gec.emergency.core.common

interface AnonymousSessionProvider {
    suspend fun getOrCreateSessionId(): String
}
