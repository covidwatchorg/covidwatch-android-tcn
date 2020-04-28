package org.covidwatch.android.data.signedreport

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.covidwatch.android.data.DateConverter
import org.covidwatch.android.data.SignedReportUploadStateConverter

@Entity(tableName = "signed_reports")
@TypeConverters(DateConverter::class, SignedReportUploadStateConverter::class)
data class SignedReport(
    @ColumnInfo(name = "end_index")
    var endIndex: Int = 0,

    @ColumnInfo(name = "is_processed")
    var isProcessed: Boolean = false,

    @ColumnInfo(name = "memo_data", typeAffinity = ColumnInfo.BLOB)
    var memoData: ByteArray = ByteArray(0),

    @ColumnInfo(name = "memo_type")
    var memoType: Int = 0,

    @ColumnInfo(name = "report_verification_public_key_bytes", typeAffinity = ColumnInfo.BLOB)
    var reportVerificationPublicKeyBytes: ByteArray = ByteArray(0),

    @PrimaryKey
    @ColumnInfo(name = "signature_bytes", typeAffinity = ColumnInfo.BLOB)
    var signatureBytes: ByteArray = ByteArray(0),

    @ColumnInfo(name = "start_index")
    var startIndex: Int = 0,

    @ColumnInfo(name = "temporary_contact_key_bytes", typeAffinity = ColumnInfo.BLOB)
    var temporaryContactKeyBytes: ByteArray = ByteArray(0),

    @ColumnInfo(name = "upload_state")
    var uploadState: UploadState = UploadState.NOTUPLOADED

) : Parcelable {

    constructor(parcel: Parcel) : this() {
        endIndex = parcel.readInt()
        isProcessed = parcel.readByte() != 0.toByte()
        memoData = parcel.createByteArray() ?: ByteArray(0)
        memoType = parcel.readInt()
        reportVerificationPublicKeyBytes = parcel.createByteArray() ?: ByteArray(0)
        signatureBytes = parcel.createByteArray() ?: ByteArray(0)
        startIndex = parcel.readInt()
        temporaryContactKeyBytes = parcel.createByteArray() ?: ByteArray(0)
    }

    constructor(signedReport: org.tcncoalition.tcnclient.crypto.SignedReport) : this() {
        endIndex = signedReport.report.j2.uShort.toInt()
        memoData = signedReport.report.memoData
        memoType = signedReport.report.memoType.ordinal
        reportVerificationPublicKeyBytes = signedReport.report.rvk.toByteArray()
        signatureBytes = signedReport.signature.toByteArray()
        startIndex = signedReport.report.j1.uShort.toInt()
        temporaryContactKeyBytes = signedReport.report.tckBytes
    }

    enum class UploadState(val code: Int) {
        NOTUPLOADED(0), UPLOADING(1), UPLOADED(2);
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(endIndex)
        parcel.writeByte(if (isProcessed) 1 else 0)
        parcel.writeByteArray(memoData)
        parcel.writeInt(memoType)
        parcel.writeByteArray(reportVerificationPublicKeyBytes)
        parcel.writeByteArray(signatureBytes)
        parcel.writeInt(startIndex)
        parcel.writeByteArray(temporaryContactKeyBytes)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignedReport

        if (endIndex != other.endIndex) return false
        if (isProcessed != other.isProcessed) return false
        if (!memoData.contentEquals(other.memoData)) return false
        if (memoType != other.memoType) return false
        if (!reportVerificationPublicKeyBytes.contentEquals(other.reportVerificationPublicKeyBytes)) return false
        if (!signatureBytes.contentEquals(other.signatureBytes)) return false
        if (startIndex != other.startIndex) return false
        if (!temporaryContactKeyBytes.contentEquals(other.temporaryContactKeyBytes)) return false
        if (uploadState != other.uploadState) return false

        return true
    }

    override fun hashCode(): Int {
        return signatureBytes.contentHashCode()
    }

    companion object CREATOR : Parcelable.Creator<SignedReport> {
        override fun createFromParcel(parcel: Parcel): SignedReport {
            return SignedReport(parcel)
        }

        override fun newArray(size: Int): Array<SignedReport?> {
            return arrayOfNulls(size)
        }
    }
}
