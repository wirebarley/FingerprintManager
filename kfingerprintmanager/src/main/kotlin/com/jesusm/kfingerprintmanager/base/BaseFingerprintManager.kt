package com.jesusm.kfingerprintmanager.base

import androidx.fragment.app.FragmentManager
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.jesusm.kfingerprintmanager.base.ui.FingerprintBaseDialogFragment.Builder
import com.jesusm.kfingerprintmanager.base.ui.System

abstract class BaseFingerprintManager(val fingerprintAssetsManager: FingerprintAssetsManager,
                                      private val system: System) {

    var authenticationDialogStyle: Int = -1

    fun showFingerprintDialog(builder: Builder<*, *>,
                              fragmentManager: FragmentManager,
                              customDescription: String,
                              callback: KFingerprintManager.FingerprintBaseCallback) {
        builder.withCallback(callback)
                .withCustomStyle(authenticationDialogStyle)
                .withFingerprintHardwareInformation(fingerprintAssetsManager)
                .withCustomDescription(customDescription)

        system.addDialogInfo(builder, fragmentManager)
        system.showDialog()
    }
}