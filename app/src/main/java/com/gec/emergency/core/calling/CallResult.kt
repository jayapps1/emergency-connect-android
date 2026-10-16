package com.gec.emergency.core.calling

sealed interface CallResult {
    data object Started : CallResult
    data object InvalidNumber : CallResult
    data object Unsupported : CallResult
    data class Failed(val reason: String?) : CallResult
}
