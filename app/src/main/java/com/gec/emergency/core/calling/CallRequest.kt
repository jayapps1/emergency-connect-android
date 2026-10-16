package com.gec.emergency.core.calling

data class CallRequest(
    val phoneNumber: String,
    val callType: CallType = CallType.GENERAL,
    val stationId: String? = null,
)
