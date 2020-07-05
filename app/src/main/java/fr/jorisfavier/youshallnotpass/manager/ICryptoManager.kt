package fr.jorisfavier.youshallnotpass.manager

import fr.jorisfavier.youshallnotpass.manager.model.EncryptedData

interface ICryptoManager {
    /**
     * The Cipher created with [getInitializedCipherForEncryption] is used here
     */
    fun encryptData(plaintext: String): EncryptedData

    /**
     * The Cipher created with [getInitializedCipherForDecryption] is used here
     */
    fun decryptData(ciphertext: ByteArray, initializationVector: ByteArray): String

}