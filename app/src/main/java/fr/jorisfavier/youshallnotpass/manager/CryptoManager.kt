package fr.jorisfavier.youshallnotpass.manager

import fr.jorisfavier.youshallnotpass.manager.model.EncryptedData

interface CryptoManager {
    /**
     * Encrypt the given data using a private encryption key
     * @param plaintext the text to be encrypted
     * @return
     */
    fun encryptData(plaintext: String): EncryptedData

    /**
     * Decrypt a given byte array using the initialization vector
     * @param ciphertext the encrypted text
     * @param initializationVector the initialization vector that has been used to encrypt the data
     * @return the decrypted data as a String
     */
    fun decryptData(ciphertext: ByteArray, initializationVector: ByteArray): String

    /**
     * Encrypt the given data using a given password
     * @param password the encryption key
     * @param data the content to be encrypted
     * @return an encrypted byte array
     */
    fun encryptDataWithPassword(password: String, data: ByteArray): ByteArray

    /**
     * Decrypt the given data using a given password
     * @param password the decryption key
     * @param encryptedData the encrypted data as a byte array
     * @return the decrypted data as ByteArray
     */
    fun decryptDataWithPassword(password: String, encryptedData: ByteArray): ByteArray

    /**
     * Encrypt the given data using a given public rsa key
     * @param key a public rsa key using the following transformation: RSA/ECB/OAEPWithSHA1AndMGF1Padding
     * @param data the data to encrypt
     * @return an encrypted byte array
     */
    fun encryptDataWithPublicKey(key: String, data: String): ByteArray

}