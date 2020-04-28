package org.covidwatch.android.data.signedreport.firestore

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.covidwatch.android.BuildConfig
import org.covidwatch.android.data.signedreport.SignedReport
import org.covidwatch.android.data.signedreport.SignedReportDAO
import org.json.JSONObject
import java.io.IOException

class SignedReportsUploader(
    private val okHttpClient: OkHttpClient,
    private val signedReportDAO: SignedReportDAO
) {

    fun startUploading() {
        GlobalScope.launch(Dispatchers.IO) {
            signedReportDAO.all().collect {
                uploadSignedReportsNeeded(it)
            }
        }
    }

    private fun uploadSignedReportsNeeded(signedReports: List<SignedReport>) {
        val signedReportsToUpload = signedReports.filter {
            it.uploadState == SignedReport.UploadState.NOTUPLOADED
        }
        uploadContactEvents(signedReportsToUpload)
    }

    private fun uploadContactEvents(signedReports: List<SignedReport>) {
        if (signedReports.isEmpty()) return
        signedReports.forEach { signedReport ->

            val signatureBytesBase64EncodedString = base64String(signedReport.signatureBytes)

            Log.i(TAG, "Uploading signed report ($signatureBytesBase64EncodedString)...")

            signedReport.uploadState = SignedReport.UploadState.UPLOADING
            signedReportDAO.update(signedReport)

            try {
                val json = signedReport.toJson()
                val isSuccessful = submitReport(json)
                if (isSuccessful) {
                    uploaded(signedReport)
                    Log.i(TAG, "Uploaded signed report ($signatureBytesBase64EncodedString)")
                } else {
                    notUploaded(signedReport)
                    Log.e(
                        TAG,
                        "Uploading signed report ($signatureBytesBase64EncodedString) failed"
                    )
                }
            } catch (e: IOException) {
                notUploaded(signedReport)
                Log.e(
                    TAG,
                    "Uploading signed report ($signatureBytesBase64EncodedString) failed",
                    e
                )
            }
        }
    }

    private fun uploaded(signedReport: SignedReport) {
        signedReport.uploadState = SignedReport.UploadState.UPLOADED
        signedReportDAO.update(signedReport)
    }

    private fun notUploaded(signedReport: SignedReport) {
        signedReport.uploadState = SignedReport.UploadState.NOTUPLOADED
        signedReportDAO.update(signedReport)
    }

    private fun SignedReport.toJson(): String {
        val data = mapOf(
            FirestoreConstants.FIELD_TEMPORARY_CONTACT_KEY_BYTES to base64String(
                temporaryContactKeyBytes
            ),
            FirestoreConstants.FIELD_START_INDEX to startIndex,
            FirestoreConstants.FIELD_END_INDEX to endIndex,
            FirestoreConstants.FIELD_MEMO_DATA to base64String(memoData),
            FirestoreConstants.FIELD_MEMO_TYPE to memoType,
            FirestoreConstants.FIELD_REPORT_VERIFICATION_PUBLIC_KEY_BYTES to base64String(
                reportVerificationPublicKeyBytes
            ),
            FirestoreConstants.FIELD_SIGNATURE_BYTES to base64String(signatureBytes)
        )

        return JSONObject(data).toString()
    }

    private fun base64String(input: ByteArray): String {
        return Base64.encodeToString(input, Base64.NO_WRAP)
    }

    @Throws(IOException::class)
    private fun submitReport(json: String): Boolean {
        val apiUrl = BuildConfig.FIREBASE_CLOUD_FUNCTIONS_ENDPOINT
        val url = "$apiUrl/submitReport"
        val body = json.toRequestBody(contentType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return okHttpClient.newCall(request).execute().use { response ->
            response.isSuccessful
        }
    }

    private fun contentType(): MediaType {
        return "application/json; charset=utf-8".toMediaType()
    }

    companion object {
        private const val TAG = "SignedReportsUploader"
    }
}
