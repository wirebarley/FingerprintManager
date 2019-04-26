package com.jesusm.kfingerprintmanager.base.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.jesusm.kfingerprintmanager.R
import com.jesusm.kfingerprintmanager.base.FingerprintAssetsManager
import com.jesusm.kfingerprintmanager.base.hardware.FingerprintHardware
import com.jesusm.kfingerprintmanager.base.ui.presenter.FingerprintBaseDialogPresenter
import kotlin.properties.Delegates

abstract class FingerprintBaseDialogFragment<T : FingerprintBaseDialogPresenter> : AppCompatDialogFragment(),
        FingerprintBaseDialogPresenter.View {

    var callback: KFingerprintManager.FingerprintBaseCallback? = null
    lateinit var dialogRootView: View
    lateinit var fingerprintContainer: View
    lateinit var tvAlert: TextView
    lateinit var alertDialog: AlertDialog
    private var customDialogStyle: Int = 0
    private var customDescription: String? = null


    var presenter by Delegates.observable<T?>(null) {
        _, _, new ->
        onPresenterChanged(new)
    }

    open fun onPresenterChanged(new: T?) {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = buildDialogContext()
        val layoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        dialogRootView = layoutInflater.inflate(R.layout.fingerprint_dialog_container, null, false)
        tvAlert = dialogRootView.findViewById(R.id.fingerprint_status)
        fingerprintContainer = dialogRootView.findViewById(R.id.fingerprint_dialog_content)
        val description = dialogRootView.findViewById(R.id.fingerprint_description) as TextView
        if (customDescription != null)
            description.text = customDescription
        inflateViews(dialogRootView)

        val builder = AlertDialog.Builder(context, customDialogStyle)
        builder.setView(dialogRootView)

        addDialogButtons(builder)

        return builder.create().apply {
            alertDialog = this
            alertDialog.setCanceledOnTouchOutside(false)
            setOnShowListener({ onDialogShown() })
        }
    }

    @CallSuper
    open fun inflateViews(rootView: View) {
    }

    @CallSuper
    open fun onDialogShown() {
        presenter?.onViewShown()
    }

    @CallSuper
    open fun addDialogButtons(dialogBuilder: AlertDialog.Builder) {
        dialogBuilder.setNegativeButton(R.string.cancel, { _, _ -> presenter?.onDialogCancelled() })
    }

    override fun onPause() {
        super.onPause()
        presenter?.pause()
    }

    private fun buildDialogContext(): Context? =
            if (customDialogStyle == 1) context else ContextThemeWrapper(context, customDialogStyle)

    override fun onFingerprintDisplayed() {
        updateDialogButtonText(DialogInterface.BUTTON_NEGATIVE, R.string.cancel)
        fingerprintContainer.visibility = View.VISIBLE
    }

    fun updateDialogButtonText(whichButton: Int, @StringRes resId: Int) {
        alertDialog.getButton(whichButton)?.setText(resId)
    }

    override fun onCancelled() {
        callback?.onCancelled()
    }

    override fun close() {
        dismissAllowingStateLoss()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)

        presenter?.onDialogCancelled()
    }

    override fun onFingerprintNotRecognized() {
        callback?.onFingerprintNotRecognized()
    }

    override fun onAuthenticationFailedWithHelp(help: String?) {
        tvAlert.text = help.toString()
    }

    override fun onFingerprintNotAvailable() {
        callback?.onFingerprintNotAvailable()
    }

    abstract class Builder<D : FingerprintBaseDialogFragment<P>, P : FingerprintBaseDialogPresenter> {
        private var customStyle: Int = 0
        private var callback: KFingerprintManager.FingerprintBaseCallback? = null
        private lateinit var fingerPrintHardware: FingerprintHardware
        private lateinit var cryptoObject: FingerprintManagerCompat.CryptoObject
        private var customDescription: String? = null

        fun withCustomDescription(customDescription: String?): Builder<*, *> {
            this.customDescription = customDescription
            return this
        }

        fun withCustomStyle(customStyle: Int): Builder<*, *> {
            this.customStyle = customStyle
            return this
        }

        fun withCallback(callback: KFingerprintManager.FingerprintBaseCallback): Builder<*, *> {
            this.callback = callback
            return this
        }

        fun withFingerprintHardwareInformation(fingerprintAssetsManager: FingerprintAssetsManager): Builder<*, *> {
            this.fingerPrintHardware = fingerprintAssetsManager.fingerprintHardware
            this.cryptoObject = fingerprintAssetsManager.getCryptoObject()!!

            return this
        }

        @Throws(RuntimeException::class)
        internal fun build(): D {
            if (callback == null) {
                throw RuntimeException("You need to provide a callback")
            }

            val dialogFragment = createDialogFragment()
            dialogFragment.callback = callback

            val presenter = createPresenter(dialogFragment)
            dialogFragment.presenter = presenter
            presenter.setFingerprintHardware(fingerPrintHardware, cryptoObject)
            if (customStyle != -1) {
                dialogFragment.customDialogStyle = customStyle
            }
            dialogFragment.customDescription = customDescription

            addProperties(dialogFragment)

            return dialogFragment
        }

        protected abstract fun createDialogFragment(): D

        protected abstract fun createPresenter(view: D): P

        protected abstract fun addProperties(dialogFragment: D)
    }
}