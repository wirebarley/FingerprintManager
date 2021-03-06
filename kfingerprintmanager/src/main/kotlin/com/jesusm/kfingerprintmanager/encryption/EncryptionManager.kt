package com.jesusm.kfingerprintmanager.encryption

import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.FragmentManager
import com.jesusm.kfingerprintmanager.KFingerprintManager
import com.jesusm.kfingerprintmanager.base.BaseFingerprintManager
import com.jesusm.kfingerprintmanager.base.FingerprintAssetsManager
import com.jesusm.kfingerprintmanager.base.ui.System
import com.jesusm.kfingerprintmanager.encryption.ui.FingerprintEncryptionDialogFragment
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.security.spec.InvalidParameterSpecException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*


class EncryptionManager(val encoder: Encoder, fingerprintAssetsManager: FingerprintAssetsManager, system: System) : BaseFingerprintManager(fingerprintAssetsManager, system) {

    fun encrypt(messageToEncrypt: String, encryptionCallback: KFingerprintManager.EncryptionCallback, customDescription: String, fragmentManager: FragmentManager) {
        if (messageToEncrypt.isEmpty()) {
            encryptionCallback.onEncryptionFailed()
            return
        }

        fingerprintAssetsManager.initSecureDependencies(
                object : KFingerprintManager.InitialisationCallback {
                    override fun onErrorFingerprintNotInitialised() {
                        encryptionCallback.onEncryptionFailed()
                    }

                    override fun onErrorFingerprintNotEnrolled() {
                        encryptionCallback.onFingerprintNotAvailable()
                    }

                    override fun onInitialisationSuccessfullyCompleted() {
                        val callback = object : KFingerprintManager.EncryptionAuthenticatedCallback {
                            override fun onAuthenticationSuccess(cryptoObject: FingerprintManagerCompat.CryptoObject) {
                                val cipher = cryptoObject.cipher
                                try {
                                    val messageToEncryptBytes = cipher?.doFinal(messageToEncrypt.toByteArray(charset("UTF-8")))
                                    val ivBytes = cipher?.parameters?.getParameterSpec(IvParameterSpec::class.java)?.iv

                                    val encryptedMessage = messageToEncryptBytes?.let { ivBytes?.let { it1 -> EncryptionData(it, it1, encoder) } }
                                    encryptedMessage?.print()?.let { encryptionCallback.onEncryptionSuccess(it) }
                                } catch (e: UnsupportedEncodingException) {
                                    encryptionCallback.onEncryptionFailed()
                                } catch (e: InvalidParameterSpecException) {
                                    encryptionCallback.onEncryptionFailed()
                                } catch (e: BadPaddingException) {
                                    encryptionCallback.onEncryptionFailed()
                                } catch (e: IllegalBlockSizeException) {
                                    encryptionCallback.onEncryptionFailed()
                                }

                            }

                            override fun onFingerprintNotRecognized() {
                                encryptionCallback.onFingerprintNotRecognized()
                            }

                            override fun onAuthenticationFailedWithHelp(help: String?) {
                                encryptionCallback.onAuthenticationFailedWithHelp(help)
                            }

                            override fun onFingerprintNotAvailable() {
                                encryptionCallback.onFingerprintNotAvailable()
                            }

                            override fun onCancelled() {
                                encryptionCallback.onCancelled()
                            }
                        }

                        val builder = FingerprintEncryptionDialogFragment.Builder()
                        showFingerprintDialog(builder, fragmentManager, customDescription, callback)
                    }

                    override fun onFingerprintNotAvailable() {
                        encryptionCallback.onFingerprintNotAvailable()
                    }
                })
    }

    fun toBytes(chars: CharArray): ByteArray {
        val charBuffer = CharBuffer.wrap(chars)
        val byteBuffer = Charset.forName("UTF-8").encode(charBuffer)
        val bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit())
        Arrays.fill(byteBuffer.array(), 0.toByte()) // clear sensitive data
        return bytes
    }

    fun encryptChar(messageCharToEncrypt: CharArray, encryptionCallback: KFingerprintManager.EncryptionCallback, customDescription: String, fragmentManager: FragmentManager) {
        if (messageCharToEncrypt.isEmpty()) {
            encryptionCallback.onEncryptionFailed()
            return
        }

        fingerprintAssetsManager.initSecureDependencies(
                object : KFingerprintManager.InitialisationCallback {
                    override fun onErrorFingerprintNotInitialised() {
                        encryptionCallback.onEncryptionFailed()
                    }

                    override fun onErrorFingerprintNotEnrolled() {
                        encryptionCallback.onFingerprintNotAvailable()
                    }

                    override fun onInitialisationSuccessfullyCompleted() {
                        val callback = object : KFingerprintManager.EncryptionAuthenticatedCallback {
                            override fun onAuthenticationSuccess(cryptoObject: FingerprintManagerCompat.CryptoObject) {
                                val cipher = cryptoObject.cipher
                                try {
                                    val messageToEncryptBytes = cipher?.doFinal(toBytes(messageCharToEncrypt))
                                    val ivBytes = cipher?.parameters?.getParameterSpec(IvParameterSpec::class.java)?.iv

                                    val encryptedMessage = messageToEncryptBytes?.let { ivBytes?.let { it1 -> EncryptionData(it, it1, encoder) } }
                                    encryptedMessage?.print()?.let { encryptionCallback.onEncryptionSuccess(it) }
                                    Arrays.fill(messageCharToEncrypt, 0x20.toChar())
                                    Arrays.fill(messageToEncryptBytes, 0.toByte())
                                } catch (e: UnsupportedEncodingException) {
                                    encryptionCallback.onEncryptionFailed()
                                } catch (e: InvalidParameterSpecException) {
                                    encryptionCallback.onEncryptionFailed()
                                } catch (e: BadPaddingException) {
                                    encryptionCallback.onEncryptionFailed()
                                } catch (e: IllegalBlockSizeException) {
                                    encryptionCallback.onEncryptionFailed()
                                }

                            }

                            override fun onFingerprintNotRecognized() {
                                encryptionCallback.onFingerprintNotRecognized()
                            }

                            override fun onAuthenticationFailedWithHelp(help: String?) {
                                encryptionCallback.onAuthenticationFailedWithHelp(help)
                            }

                            override fun onFingerprintNotAvailable() {
                                encryptionCallback.onFingerprintNotAvailable()
                            }

                            override fun onCancelled() {
                                encryptionCallback.onCancelled()
                            }
                        }

                        val builder = FingerprintEncryptionDialogFragment.Builder()
                        showFingerprintDialog(builder, fragmentManager, customDescription, callback)
                    }

                    override fun onFingerprintNotAvailable() {
                        encryptionCallback.onFingerprintNotAvailable()
                    }
                })
    }

    fun bytesToChars(bytes: ByteArray): CharArray {
        val charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes))
        return Arrays.copyOf(charBuffer.array(), charBuffer.limit())
    }

    fun decryptChar(messageToDecrypt: String, callback: KFingerprintManager.DecryptionCharCallback, customDescription: String, fragmentManager: androidx.fragment.app.FragmentManager) {
        if (messageToDecrypt.isEmpty()) {
            callback.onDecryptionFailed()
            return
        }

        val decryptionData = EncryptionData(messageToDecrypt, encoder)
        if (decryptionData.dataIsCorrect().not()) {
            callback.onDecryptionFailed()
            return
        }

        fingerprintAssetsManager.initSecureDependenciesForDecryption(object : KFingerprintManager.InitialisationCallback {
            override fun onErrorFingerprintNotInitialised() {
                callback.onDecryptionFailed()
            }

            override fun onErrorFingerprintNotEnrolled() {
                callback.onFingerprintNotAvailable()
            }

            override fun onInitialisationSuccessfullyCompleted() {
                val decryptionCallback = object : KFingerprintManager.EncryptionAuthenticatedCallback {
                    override fun onAuthenticationSuccess(cryptoObject: FingerprintManagerCompat.CryptoObject) {
                        try {
                            val cipher = cryptoObject.cipher

                            val encryptedMessage = decryptionData.decodedMessage()
                            val decryptedMessageBytes = cipher?.doFinal(encryptedMessage)
                            decryptedMessageBytes?.let {
                                callback.onDecryptionCharSuccess(bytesToChars(it))
                            }
                        } catch (e: IllegalBlockSizeException) {
                            callback.onDecryptionFailed()
                        } catch (e: BadPaddingException) {
                            callback.onDecryptionFailed()
                        }
                    }

                    override fun onFingerprintNotRecognized() {
                        callback.onFingerprintNotRecognized()
                    }

                    override fun onAuthenticationFailedWithHelp(help: String?) {
                        callback.onAuthenticationFailedWithHelp(help)
                    }

                    override fun onFingerprintNotAvailable() {
                        callback.onFingerprintNotAvailable()
                    }

                    override fun onCancelled() {
                        callback.onCancelled()
                    }
                }

                val builder = FingerprintEncryptionDialogFragment.Builder()
                showFingerprintDialog(builder, fragmentManager, customDescription, decryptionCallback)
            }

            override fun onFingerprintNotAvailable() {
                callback.onFingerprintNotAvailable()
            }
        }, decryptionData.decodedIVs())
    }

    fun decrypt(messageToDecrypt: String, callback: KFingerprintManager.DecryptionCallback, customDescription: String, fragmentManager: androidx.fragment.app.FragmentManager) {
        if (messageToDecrypt.isEmpty()) {
            callback.onDecryptionFailed()
            return
        }

        val decryptionData = EncryptionData(messageToDecrypt, encoder)
        if (decryptionData.dataIsCorrect().not()) {
            callback.onDecryptionFailed()
            return
        }

        fingerprintAssetsManager.initSecureDependenciesForDecryption(object : KFingerprintManager.InitialisationCallback {
            override fun onErrorFingerprintNotInitialised() {
                callback.onDecryptionFailed()
            }

            override fun onErrorFingerprintNotEnrolled() {
                callback.onFingerprintNotAvailable()
            }

            override fun onInitialisationSuccessfullyCompleted() {
                val decryptionCallback = object : KFingerprintManager.EncryptionAuthenticatedCallback {
                    override fun onAuthenticationSuccess(cryptoObject: FingerprintManagerCompat.CryptoObject) {
                        try {
                            val cipher = cryptoObject.cipher

                            val encryptedMessage = decryptionData.decodedMessage()
                            val decryptedMessageBytes = cipher?.doFinal(encryptedMessage)
                            val decryptedMessage = decryptedMessageBytes?.let { String(it) }

                            decryptedMessage?.let { callback.onDecryptionSuccess(it) }
                        } catch (e: IllegalBlockSizeException) {
                            callback.onDecryptionFailed()
                        } catch (e: BadPaddingException) {
                            callback.onDecryptionFailed()
                        }
                    }

                    override fun onFingerprintNotRecognized() {
                        callback.onFingerprintNotRecognized()
                    }

                    override fun onAuthenticationFailedWithHelp(help: String?) {
                        callback.onAuthenticationFailedWithHelp(help)
                    }

                    override fun onFingerprintNotAvailable() {
                        callback.onFingerprintNotAvailable()
                    }

                    override fun onCancelled() {
                        callback.onCancelled()
                    }
                }

                val builder = FingerprintEncryptionDialogFragment.Builder()
                showFingerprintDialog(builder, fragmentManager, customDescription, decryptionCallback)
            }

            override fun onFingerprintNotAvailable() {
                callback.onFingerprintNotAvailable()
            }
        }, decryptionData.decodedIVs())
    }
}
