package com.jesusm.kfingerprintmanager.base.ui

import android.support.v4.app.FragmentManager
import android.view.WindowManager

class SystemImpl : System {
    private val FINGERPRINT_DIALOG_TAG = "KFingerprintManager:fingerprintDialog"

    private var fingerprintBaseDialogFragment: FingerprintBaseDialogFragment<*>? = null
    private var dialogFragmentManager: FragmentManager? = null

    override fun showDialog() {
        try {
            fingerprintBaseDialogFragment?.show(dialogFragmentManager, FINGERPRINT_DIALOG_TAG)
        } catch (e: WindowManager.BadTokenException) {
        }
    }

    override fun addDialogInfo(builder: FingerprintBaseDialogFragment.Builder<out FingerprintBaseDialogFragment<*>, *>?,
                               fragmentManager: FragmentManager?) {
        builder?.let {
            fingerprintBaseDialogFragment = it.build()
            dialogFragmentManager = fragmentManager
        }
    }
}