package fr.jorisfavier.youshallnotpass.manager.impl

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.manager.model.EncryptedData
import fr.jorisfavier.youshallnotpass.utils.md5
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec

class CryptoManager : ICryptoManager {

    override fun encryptData(plaintext: String): EncryptedData {
        val cipher = getInitializedCipherForEncryption()
        val cipherText = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return EncryptedData(cipherText, cipher.iv)
    }

    override fun decryptData(ciphertext: ByteArray, initializationVector: ByteArray): String {
        val cipher = getInitializedCipherForDecryption(initializationVector)
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }

    override fun encryptDataWithPassword(password: String, data: ByteArray): ByteArray {
        val pbParamSpec = PBEParameterSpec(password.md5(), COUNT)
        val pbKeySpec = PBEKeySpec(password.toCharArray())
        val secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
        val key = secretKeyFactory.generateSecret(pbKeySpec)
        val cipher = Cipher.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, pbParamSpec)
        return cipher.doFinal(data)
    }

    override fun decryptDataWithPassword(password: String, encryptedData: ByteArray): ByteArray {
        val pbParamSpec = PBEParameterSpec(password.md5(), COUNT)
        val pbKeySpec = PBEKeySpec(password.toCharArray())
        val secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
        val key = secretKeyFactory.generateSecret(pbKeySpec)
        val cipher = Cipher.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, pbParamSpec)
        return cipher.doFinal(encryptedData)
    }

    /**
     * This method first gets or generates an instance of SecretKey and then initializes the Cipher
     * with the key. The secret key uses [ENCRYPT_MODE][Cipher.ENCRYPT_MODE] is used.
     */
    private fun getInitializedCipherForEncryption(): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    /**
     * This method first gets or generates an instance of SecretKey and then initializes the Cipher
     * with the key. The secret key uses [DECRYPT_MODE][Cipher.DECRYPT_MODE] is used.
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
        keyStore.getKey(KEY_NAME, null)?.let {
            return it as SecretKey
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