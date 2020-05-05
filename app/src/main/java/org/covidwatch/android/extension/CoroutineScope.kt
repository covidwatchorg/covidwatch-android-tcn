package org.covidwatch.android.extension

import org.covidwatch.android.functional.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.covidwatch.android.ENStatus
import org.covidwatch.android.domain.UseCase

fun <T : Any?> CoroutineScope.io(block: suspend () -> T, result: (T) -> Unit) {
    launch {
        result(
            withContext(Dispatchers.IO) {
                block()
            }
        )
    }
}

fun <T : Any?> CoroutineScope.io(block: suspend () -> T) {
    launch {
        withContext(Dispatchers.IO) {
            block()
        }
    }
}

fun <Type : Any, Params> CoroutineScope.launchUseCase(
    useCase: UseCase<Type, Params>,
    params: Params? = null,
    onResult: (Either<ENStatus, Type>) -> Unit = {}
) {
    useCase(this, params, onResult)
}