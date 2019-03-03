package fr.jorisfavier.youshallnotpass.managers.support

import android.content.Context
import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import fr.jorisfavier.youshallnotpass.managers.IFingerPrintAuthManager
import java.lang.Exception
import java.security.*
import javax.crypto.KeyGenerator
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.inject.Inject


class FingerPrintAuthManager @Inject constructor(): IFingerPrintAuthManager {

    val KEY_STORE_NAME = "YouShallNotPass"
    var keyStore: KeyStore? = null
    var cipher: Cipher? = null

    private fun generateKey() {
        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore!!.load(null)

            var keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(KeyGenParameterSpec.Builder(KEY_STORE_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build())

            keyGenerator.generateKey()

        } catch (exc: Exception) {
            exc.printStackTrace()
        }

    }

    private fun initCipher(): Boolean {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7)

        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        keyStore?.let {
            try {
                it.load(
                        null)
                val key = it.getKey(KEY_STORE_NAME, null) as SecretKey
                cipher!!.init(Cipher.ENCRYPT_MODE, key)
                return true
            } catch (e: Exception) {
                return false
            }
        }
        return false
    }

    override fun fingerPrintAuth(context: Context, callback: FingerprintManagerCompat.AuthenticationCallback){
        generateKey()
        initCipher()
        val cryptoObject = FingerprintManagerCompat.CryptoObject(cipher!!)
        val fingerprintManagerCompat = FingerprintManagerCompat.from(context)

        fingerprintManagerCompat.authenticate(cryptoObject,0,CancellationSignal(),callback,null)
    }
}