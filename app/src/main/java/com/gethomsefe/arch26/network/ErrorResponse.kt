package com.gethomsefe.arch26.network

sealed interface ErrorResponse<out T> {
    data class Server<T>(val body: T, val code: Int) : ErrorResponse<T>
    data class Network(val throwable: Throwable) : ErrorResponse<Nothing>
    data class Unexpected(val throwable: Throwable) : ErrorResponse<Nothing>
}
