package com.gec.emergency.core.location

interface LocationProvider {
    suspend fun getCurrentLocation(): LocationResult
}
