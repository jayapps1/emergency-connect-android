package com.gec.emergency.core.location

import com.gec.emergency.domain.model.GeoLocation

sealed interface LocationResult {
    data class Success(val location: GeoLocation) : LocationResult
    data object PermissionDenied : LocationResult
    data object LocationDisabled : LocationResult
    data object Unavailable : LocationResult
    data class Error(val message: String?) : LocationResult
}
