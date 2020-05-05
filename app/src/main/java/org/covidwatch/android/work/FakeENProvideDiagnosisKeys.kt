package org.covidwatch.android.work

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class FakeENProvideDiagnosisKeys(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        Intent().also { intent ->
            intent.action =
                "com.google.android.gms.exposurenotification.ACTION_EXPOSURE_STATE_UPDATED"
            context.sendBroadcast(intent)
        }
        return Result.success()
    }
}