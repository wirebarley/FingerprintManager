package com.jesusm.kfingerprintmanager.base.model

import androidx.core.os.CancellationSignal

class FingerprintManagerCancellationSignal(val cancellationSignal: CancellationSignal = CancellationSignal()) {
    var isCancelled : Boolean = false

    fun start() {
        cancel()
        isCancelled = false
    }

    fun cancel() {
        if (isCancelled.not()) {
            cancellationSignal.cancel()
            isCancelled = true
        }
    }
}