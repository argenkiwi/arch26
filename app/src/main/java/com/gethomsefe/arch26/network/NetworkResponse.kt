package com.gethomsefe.arch26.network

import arrow.core.Either

typealias NetworkResponse<T, E> = Either<ErrorResponse<E>, SuccessResponse<T>>