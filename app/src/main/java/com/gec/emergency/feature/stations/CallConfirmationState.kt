package com.gec.emergency.feature.stations

import com.gec.emergency.core.calling.CallRequest

data class CallConfirmationState(
    val isVisible: Boolean = false,
    val stationName: String = "",
    val phoneNumber: String = "",
    val contactTypeLabel: String = "",
    val callRequest: CallRequest? = null,
)
