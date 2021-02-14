package fr.jorisfavier.youshallnotpass.manager.impl

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.manager.model.EncryptedData
import fr.jorisfavier.youshallnotpass.utils.md5
import java.io.IOException
import java.nio.charset.Charset
import java.security.*
import java.security.cert.CertificateException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec

class CryptoManager : ICryptoManager {

    @Throws(
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        CertificateException::class,
        IOException::class,
        KeyStoreException::class,
        UnrecoverableKeyException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class
    )
    override fun encryptData(plaintext: String): EncryptedData {
        val cipher = getInitializedCipherForEncryption()
        val cipherText = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return EncryptedData(cipherText, cipher.iv)
    }

    @Throws(
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        CertificateException::class,
        IOException::class,
        KeyStoreException::class,
        UnrecoverableKeyException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class
    )
    override fun decryptData(ciphertext: ByteArray, initializationVector: ByteArray): String {
        val cipher = getInitializedCipherForDecryption(initializationVector)
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }

    @Throws(
        NoSuchAlgorithmException::class,
        InvalidKeySpecException::class,
        NoSuchPaddingException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class
    )
    override fun encryptDataWithPassword(password: String, data: ByteArray): ByteArray {
        val pbParamSpec = PBEParameterSpec(password.md5(), COUNT)
        val pbKeySpec = PBEKeySpec(password.toCharArray())
        val secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
        val key = secretKeyFactory.generateSecret(pbKeySpec)
        val cipher = Cipher.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, pbParamSpec)
        return cipher.doFinal(data)
    }

    @Throws(
        NoSuchAlgorithmException::class,
        InvalidKeySpecException::class,
        NoSuchPaddingException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class
    )
    override fun decryptDataWithPassword(password: String, encryptedData: ByteArray): ByteArray {
        val pbParamSpec = PBEParameterSpec(password.md5(), COUNT)
        val pbKeySpec = PBEKeySpec(password.toCharArray())
        val secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
        val key = secretKeyFactory.generateSecret(pbKeySpec)
        val cipher = Cipher.getInstance(ENCRYPTION_WITH_PASSWORD_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, pbParamSpec)
        return cipher.doFinal(encryptedData)
    }

    override fun encryptDataWithPublicKey(key: String, data: String): ByteArray {
        val decodedKey = Base64.decode(key, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(decodedKey)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val pubKey: PublicKey = keyFactory.generatePublic(keySpec)
        val cipher: Cipher =
            Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding") //or try with "RSA"
        cipher.init(Cipher.ENCRYPT_MODE, pubKey)
        return cipher.doFinal(data.toByteArray())
    }

    /**
     * This method first gets or generates an instance of SecretKey and then initializes the Cipher
     * with the key. The secret key uses [ENCRYPT_MODE][Cipher.ENCRYPT_MODE] is used.
     */
    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        CertificateException::class,
        NoSuchAlgorithmException::class,
        IOException::class,
        KeyStoreException::class,
        UnrecoverableKeyException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class,
    )
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
    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        CertificateException::class,
        IOException::class,
        KeyStoreException::class,
        UnrecoverableKeyException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class
    )
    private fun getInitializedCipherForDecryption(initializationVector: ByteArray): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        return cipher
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class)
    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    @Throws(
        CertificateException::class,
        IOException::class,
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        UnrecoverableKeyException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class
    )
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