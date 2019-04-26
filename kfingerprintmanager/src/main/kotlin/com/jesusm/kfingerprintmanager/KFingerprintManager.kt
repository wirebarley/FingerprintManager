package com.jesusm.kfingerprintmanager

import android.content.Context
import androidx.annotation.StyleRes
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.jesusm.kfingerprintmanager.authentication.AuthenticationManager
import com.jesusm.kfingerprintmanager.base.FingerprintAssetsManager
import com.jesusm.kfingerprintmanager.base.ui.System
import com.jesusm.kfingerprintmanager.base.ui.SystemImpl
import com.jesusm.kfingerprintmanager.encryption.Base64Encoder
import com.jesusm.kfingerprintmanager.encryption.Encoder
import com.jesusm.kfingerprintmanager.encryption.EncryptionManager

class KFingerprintManager @JvmOverloads constructor(context: Context,
                                                    keyStoreAlias: String,
                                                    system: System = SystemImpl(),
                                                    fingerprintAssetsManager: FingerprintAssetsManager = FingerprintAssetsManager(context, keyStoreAlias),
                                                    encoder: Encoder = Base64Encoder(),
                                                    val authenticationManager: AuthenticationManager = AuthenticationManager(fingerprintAssetsManager, system),
                                                    val encryptionManager: EncryptionManager = EncryptionManager(encoder, fingerprintAssetsManager, system)) {

    fun setAuthenticationDialogStyle(@StyleRes styleRes: Int) {
        authenticationManager.authenticationDialogStyle = styleRes
        encryptionManager.authenticationDialogStyle = styleRes
    }

    fun encrypt(messageToEncrypt: String, callback: EncryptionCallback,
                customDescription: String,
                fragmentManager: androidx.fragment.app.FragmentManager) =
            encryptionManager.encrypt(messageToEncrypt, callback, customDescription, fragmentManager)

    fun decrypt(messageToDecrypt: String, callback: DecryptionCallback,
                customDescription: String,
                fragmentManager: androidx.fragment.app.FragmentManager) =
            encryptionManager.decrypt(messageToDecrypt, callback, customDescription, fragmentManager)

    fun authenticate(authenticationCallback: AuthenticationCallback,
                     customDescription: String,
                     fragmentManager: androidx.fragment.app.FragmentManager) =
            authenticationManager.startAuthentication(authenticationCallback, customDescription, fragmentManager)

    fun checkAvailable(initialisationCallback: InitialisationCallback) =
            authenticationManager.checkAvailable(initialisationCallback)

    interface FingerprintBaseCallback {
        fun onFingerprintNotRecognized()

        fun onAuthenticationFailedWithHelp(help: String?)

        fun onFingerprintNotAvailable()

        fun onCancelled()
    }

    interface DecryptionCallback : FingerprintBaseCallback {
        fun onDecryptionSuccess(messageDecrypted: String)

        fun onDecryptionFailed()
    }

    interface EncryptionCallback : FingerprintBaseCallback {
        fun onEncryptionSuccess(messageEncrypted: String)

        fun onEncryptionFailed()
    }

    interface AuthenticationCallback : KFingerprintManager.FingerprintBaseCallback {
        fun onAuthenticationSuccess()

        fun onSuccessWithManualPassword(password: String)
    }

    interface EncryptionAuthenticatedCallback : FingerprintBaseCallback {
        fun onAuthenticationSuccess(cryptoObject: FingerprintManagerCompat.CryptoObject)
    }

    interface InitialisationCallback : FingerprintAvailabilityCallback {
        fun onErrorFingerprintNotInitialised()

        fun onErrorFingerprintNotEnrolled()

        fun onInitialisationSuccessfullyCompleted()
    }

    interface FingerprintAvailabilityCallback {
        fun onFingerprintNotAvailable()
    }
}