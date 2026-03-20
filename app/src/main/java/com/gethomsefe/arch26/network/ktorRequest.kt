package com.gethomsefe.arch26.network

import arrow.core.raise.catch
import arrow.core.raise.either
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import java.io.IOException

suspend inline fun <reified S, reified E> ktorRequest(block: () -> HttpResponse) = either {
    catch(block) {
        when (it) {
            is ResponseException -> with(it.response) {
                raise(ErrorResponse.Server(body<E>(), status.value))
            }

            is IOException -> raise(ErrorResponse.Network(it))
            else -> raise(ErrorResponse.Unexpected(it))
        }
    }.let {
        when (it.status.value) {
            204 -> SuccessResponse.NoContent
            else -> SuccessResponse.OK(it.body<S>())
        }
    }
}