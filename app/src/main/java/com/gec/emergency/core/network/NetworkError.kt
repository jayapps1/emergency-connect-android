package com.gec.emergency.core.network

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed interface NetworkError {
    data object NoConnection : NetworkError
    data object Timeout : NetworkError
    data class ServerError(val code: Int, val message: String?) : NetworkError
    data object NotFound : NetworkError
    data object Unauthorized : NetworkError
    data class Unknown(val cause: Throwable?) : NetworkError
}

fun Throwable.toNetworkError(): NetworkError {
    return when (this) {
        is UnknownHostException, is ConnectException -> NetworkError.NoConnection
        is SocketTimeoutException -> NetworkError.Timeout
        is HttpException -> {
            when (code()) {
                401 -> NetworkError.Unauthorized
                404 -> NetworkError.NotFound
                in 500..599 -> NetworkError.ServerError(code(), message())
                else -> NetworkError.Unknown(this)
            }
        }
        is IOException -> NetworkError.NoConnection
        else -> NetworkError.Unknown(this)
    }
}
