package com.gethomsefe.arch26.network

sealed interface SuccessResponse<T> {
    val body: T

    data class OK<T>(override val body: T) : SuccessResponse<T>
    data object NoContent : SuccessResponse<Nothing> {
        override val body: Nothing
            get() = throw UnsupportedOperationException("Attempted to get body from No Content response.")
    }
}
