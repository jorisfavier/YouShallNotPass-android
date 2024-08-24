package fr.jorisfavier.youshallnotpass.manager.impl

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.manager.model.EncryptedData
import fr.jorisfavier.youshallnotpass.utils.extensions.md5
import fr.jorisfavier.youshallnotpass.utils.extensions.suspendRunCatching
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec
import kotlin.coroutines.CoroutineContext

class CryptoManagerImpl(
    private val ioDispatcher: CoroutineContext,
) : CryptoManager {

    override suspend fun encryptData(plaintext: String): Result<EncryptedData> {
        return suspendRunCatching(
            errorMessage = "An error occurred while encrypting data",
        ) {
            withContext(ioDispatcher) {
                val cipher = getInitializedCipherForEncryption()
                val cipherText = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
                EncryptedData(cipherText, cipher.iv)
            }
        }
    }

    override suspend fun decryptData(
        ciphertext: ByteArray,
        initializationVector: ByteArray,
    ): Result<String> {
        return suspendRunCatching(
            errorMessage = "An error occurred while decrypting data",
        ) {
            withContext(ioDispatcher) {
                val cipher = getInitializedCipherForDecryption(initializationVector)
                val plaintext = cipher.doFinal(ciphertext)
                String(plaintext, Charsets.UTF_8)
            }
        }
    }

    override suspend fun encryptDataWithPassword(
        password: String,
        data: ByteArray,
    ): Result<ByteArray> {
        return suspendRunCatching(
            errorMessage = "An error occurred while encrypting data with a given password",
        ) {
            withContext(ioDispatcher) {
                val pbParamSpec = PBEParameterSpec(password.md5(), COUNT)
                val pbKeySpec = PBEKeySpec(password.toCharArray())
                val secretKeyFactory =
                    SecretKeyFactory.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
                val key = secretKeyFactory.generateSecret(pbKeySpec)
                val cipher = Cipher.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
                cipher.init(Cipher.ENCRYPT_MODE, key, pbParamSpec)
                cipher.doFinal(data)
            }
        }
    }

    override suspend fun decryptDataWithPassword(
        password: String,
        encryptedData: ByteArray,
    ): Result<ByteArray> {
        return suspendRunCatching(
            errorMessage = "An error occurred while decrypting data with a given password",
        ) {
            withContext(ioDispatcher) {
                val pbParamSpec = PBEParameterSpec(password.md5(), COUNT)
                val pbKeySpec = PBEKeySpec(password.toCharArray())
                val secretKeyFactory =
                    SecretKeyFactory.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
                val key = secretKeyFactory.generateSecret(pbKeySpec)
                val cipher = Cipher.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
                cipher.init(Cipher.DECRYPT_MODE, key, pbParamSpec)
                cipher.doFinal(encryptedData)
            }
        }
    }

    override suspend fun encryptDataWithPublicKey(key: String, data: String): Result<ByteArray> {
        return suspendRunCatching(
            errorMessage = "An error occurred while encrypting data with a given public key",
        ) {
            withContext(ioDispatcher) {
                val decodedKey = Base64.decode(key, Base64.DEFAULT)
                val keySpec = X509EncodedKeySpec(decodedKey)
                val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
                val pubKey: PublicKey = keyFactory.generatePublic(keySpec)
                val cipher: Cipher =
                    Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding")
                cipher.init(Cipher.ENCRYPT_MODE, pubKey)
                cipher.doFinal(data.toByteArray())
            }
        }
    }

    /**
     * This method first gets or generates an instance of SecretKey and then initializes the Cipher
     * with the key. The secret key uses [Cipher.ENCRYPT_MODE].
     */
    private fun getInitializedCipherForEncryption(): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    /**
     * This method first gets or generates an instance of SecretKey and then initializes the Cipher
     * with the key. The secret key uses [Cipher.DECRYPT_MODE].
     */
    private fun getInitializedCipherForDecryption(initializationVector: ByteArray): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        return cipher
    }

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        // If Secretkey was previously created for that keyName, then grab and return it.
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        // Keystore must be loaded before it can be accessed
        keyStore.load(null)
        val secretKey = keyStore.getKey(KEY_NAME, null)
        if (secretKey != null) {
            return secretKey as SecretKey
        }

        // if you reach here, then a new SecretKey must be generated
        val paramsBuilder = KeyGenParameterSpec.Builder(
            KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(false)
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    companion object {
        private const val KEY_SIZE: Int = 256
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val ENCRYPTION_WITH_PASSWORD_ALGORITHM = "PBEWITHSHA256AND256BITAES-CBC-BC"
        private const val KEY_NAME = "ysnp_encryption_key"
        private const val COUNT = 999
    }
}
