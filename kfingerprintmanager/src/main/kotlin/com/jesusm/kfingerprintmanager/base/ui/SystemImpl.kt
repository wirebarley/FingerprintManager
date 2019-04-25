package com.jesusm.kfingerprintmanager.base.ui

import androidx.fragment.app.FragmentManager
import android.view.WindowManager

class SystemImpl : System {
    private val FINGERPRINT_DIALOG_TAG = "KFingerprintManager:fingerprintDialog"

    private var fingerprintBaseDialogFragment: FingerprintBaseDialogFragment<*>? = null
    private var dialogFragmentManager: androidx.fragment.app.FragmentManager? = null

    override fun showDialog() {
        try {
            fingerprintBaseDialogFragment?.show(dialogFragmentManager, FINGERPRINT_DIALOG_TAG)
        } catch (e: WindowManager.BadTokenException) {
        }
    }

    override fun addDialogInfo(builder: FingerprintBaseDialogFragment.Builder<out FingerprintBaseDialogFragment<*>, *>?,
                               fragmentManager: androidx.fragment.app.FragmentManager?) {
        builder?.let {
            fingerprintBaseDialogFragment = it.build()
            dialogFragmentManager = fragmentManager
        }
    }
}