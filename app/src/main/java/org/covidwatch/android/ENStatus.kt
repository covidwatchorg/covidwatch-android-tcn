package org.covidwatch.android

import androidx.annotation.IntDef

sealed class ENStatus {
    object SUCCESS : ENStatus()
    object FailedRejectedOptIn : ENStatus()
    object FailedServiceDisabled : ENStatus()
    object FailedBluetoothScanningDisabled : ENStatus()
    object FailedTemporarilyDisabled : ENStatus()
    object FailedInsufficientStorage : ENStatus()
    object FailedInternal : ENStatus()
    companion object {
        operator fun invoke(@Status status: Int?) = when (status) {
            Status.SUCCESS -> SUCCESS
            Status.FAILED_REJECTED_OPT_IN -> FailedRejectedOptIn
            Status.FAILED_SERVICE_DISABLED -> FailedServiceDisabled
            Status.FAILED_BLUETOOTH_SCANNING_DISABLED -> FailedBluetoothScanningDisabled
            Status.FAILED_TEMPORARILY_DISABLED -> FailedTemporarilyDisabled
            Status.FAILED_INSUFFICENT_STORAGE -> FailedInsufficientStorage
            Status.FAILED_INTERNAL -> FailedInternal
            else -> FailedInternal
        }
    }
}

//TODO: Replace with real status constants from Nearby when they are available
@IntDef
internal annotation class Status {
    companion object {
        var SUCCESS = 0
        var FAILED_REJECTED_OPT_IN = 1
        var FAILED_SERVICE_DISABLED = 2
        var FAILED_BLUETOOTH_SCANNING_DISABLED = 3
        var FAILED_TEMPORARILY_DISABLED = 4
        var FAILED_INSUFFICENT_STORAGE = 5
        var FAILED_INTERNAL = 6
    }
}