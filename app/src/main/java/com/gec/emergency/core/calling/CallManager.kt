package com.gec.emergency.core.calling

interface CallManager {
    suspend fun placeCall(request: CallRequest): CallResult
}
