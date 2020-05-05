package org.covidwatch.android.domain

import org.covidwatch.android.functional.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.covidwatch.android.exposurenotification.ENStatus

/**
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This abstraction represents an execution unit for different use cases (this means than any use
 * case in the application should implement this contract).
 *
 * By convention each [UseCase] implementation will execute its job in a background thread
 * (kotlin coroutine) and will post the result in the UI thread.
 */
abstract class UseCase<Type, in Params>(
    private val dispatchers: AppCoroutineDispatchers
) {

    abstract suspend fun run(params: Params? = null): Either<ENStatus, Type>

    operator fun invoke(
        scope: CoroutineScope,
        params: Params? = null,
        onResult: (Either<ENStatus, Type>) -> Unit = {}
    ) {
        val job = scope.async(dispatchers.io) { run(params) }
        scope.launch(dispatchers.main) { onResult(job.await()) }
    }
}