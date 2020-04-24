package org.covidwatch.android.data.signedreport.firestore

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cafe.cryptography.ed25519.Ed25519PublicKey
import cafe.cryptography.ed25519.Ed25519Signature
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.covidwatch.android.R
import org.covidwatch.android.data.CovidWatchDatabase
import org.covidwatch.android.data.TemporaryContactNumberDAO
import org.tcncoalition.tcnclient.crypto.KeyIndex
import org.tcncoalition.tcnclient.crypto.MemoType
import org.tcncoalition.tcnclient.crypto.Report
import org.tcncoalition.tcnclient.crypto.SignedReport
import java.util.*

class SignedReportsDownloadWorker(var context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private var result = Result.failure()

    override fun doWork(): Result {

        Log.i(TAG, "Downloading signed reports...")

        val now = Date()

        val lastFetchTime = FirestoreConstants.lastFetchTime()
        var fetchSinceTime = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).getLong(
            context.getString(R.string.preference_last_temporary_contact_numbers_download_date),
            lastFetchTime.time
        )
        if (fetchSinceTime < lastFetchTime.time) {
            fetchSinceTime = lastFetchTime.time
        }

        val task =
            FirebaseFirestore.getInstance().collection(FirestoreConstants.COLLECTION_SIGNED_REPORTS)
                .whereGreaterThan(
                    FirestoreConstants.FIELD_TIMESTAMP,
                    Timestamp(Date(fetchSinceTime))
                )
                .get()
                .continueWith(
                    CovidWatchDatabase.databaseWriteExecutor,
                    Continuation<QuerySnapshot, Result> { task ->
                        //result = Result.success()
                        val queryDocumentSnapshots = task.result
                        if (queryDocumentSnapshots != null) {
                            Log.i(
                                TAG,
                                "Downloaded ${queryDocumentSnapshots.size()} signed report(s)"
                            )
                            result = try {
                                queryDocumentSnapshots.documentChanges.filter {
                                    it.type == DocumentChange.Type.ADDED
                                }
                                markLocalTemporaryContactNumbers(
                                    queryDocumentSnapshots.documentChanges,
                                    true
                                )

                                with(
                                    context.getSharedPreferences(
                                        context.getString(R.string.preference_file_key),
                                        Context.MODE_PRIVATE
                                    ).edit()
                                ) {
                                    putLong(
                                        context.getString(R.string.preference_last_temporary_contact_numbers_download_date),
                                        now.time
                                    )
                                    commit()
                                }

                                Result.success()

                            } catch (exception: Exception) {
                                Result.failure()
                            }
                        } else {
                            result = Result.failure()
                        }
                        null
                    })

        Tasks.await(task)

        Log.i(TAG, "Finish task")

        return result
    }

    private fun markLocalTemporaryContactNumbers(
        documentChanges: List<DocumentChange>,
        wasPotentiallyInfectious: Boolean
    ) {
        if (documentChanges.isEmpty()) return

        val temporaryContactNumberDAO: TemporaryContactNumberDAO =
            CovidWatchDatabase.getInstance(context).temporaryContactNumberDAO()

        documentChanges.forEach { documentChange ->
            val data = documentChange.document
            val temporaryContactKeyBytes =
                (data[FirestoreConstants.FIELD_TEMPORARY_CONTACT_KEY_BYTES] as? Blob)?.toBytes()
                    ?: return@forEach
            val endIndex =
                (data[FirestoreConstants.FIELD_END_INDEX] as? Long)?.toShort() ?: return@forEach
            val memoData =
                (data[FirestoreConstants.FIELD_MEMO_DATA] as? Blob)?.toBytes() ?: return@forEach
            val memoType =
                (data[FirestoreConstants.FIELD_MEMO_TYPE] as? Long)?.toShort() ?: return@forEach
            val reportVerificationPublicKeyBytes =
                (data[FirestoreConstants.FIELD_REPORT_VERIFICATION_PUBLIC_KEY_BYTES] as? Blob)?.toBytes()
                    ?: return@forEach
            val signatureBytes =
                (data[FirestoreConstants.FIELD_SIGNATURE_BYTES] as? Blob)?.toBytes()
                    ?: return@forEach
            val startIndex =
                (data[FirestoreConstants.FIELD_START_INDEX] as? Long)?.toShort() ?: return@forEach

            val report = Report(
                Ed25519PublicKey.fromByteArray(reportVerificationPublicKeyBytes),
                temporaryContactKeyBytes,
                KeyIndex(startIndex),
                KeyIndex(endIndex),
                MemoType.fromByteArray(arrayOf(memoType.toByte()).toByteArray()),
                memoData
            )

            val signedReport = SignedReport(report, Ed25519Signature.fromByteArray(signatureBytes))
            val signatureBase64EncodedString = Base64.encodeToString(signatureBytes, Base64.NO_WRAP)

            try {
                signedReport.verify()
                Log.i(
                    TAG,
                    "Source integrity verification for signed report ($signatureBase64EncodedString) succeeded"
                )
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Source integrity verification for signed report ($signatureBase64EncodedString) failed"
                )
                return@forEach
            }

            val recomputedTemporaryContactNumbers = report.temporaryContactNumbers
            val identifiers = mutableListOf<ByteArray>()
            recomputedTemporaryContactNumbers.forEach {
                identifiers.add(it.bytes)
            }

            Log.i(
                TAG,
                "Marking ${identifiers.size} temporary contact number(s) as potentially infectious=$wasPotentiallyInfectious ..."
            )
            val chunkSize = 998 // SQLITE_MAX_VARIABLE_NUMBER - 1
            identifiers.chunked(chunkSize).forEach {
                temporaryContactNumberDAO.update(it, wasPotentiallyInfectious)
                Log.i(
                    TAG,
                    "Marked ${it.size} temporary contact number(s) as potentially infectious=$wasPotentiallyInfectious"
                )
            }
        }
    }

    companion object {
        private const val TAG = "ReportsDownloadWorker"
        const val WORKER_NAME = "org.covidwatch.android.refresh"
    }
}