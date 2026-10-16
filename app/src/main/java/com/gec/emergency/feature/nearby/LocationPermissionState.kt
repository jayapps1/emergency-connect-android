package com.gec.emergency.feature.nearby

sealed interface LocationPermissionState {
    data object Unknown : LocationPermissionState
    data object Granted : LocationPermissionState
    data object Denied : LocationPermissionState
    data object PermanentlyDenied : LocationPermissionState
}
