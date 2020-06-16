package fr.jorisfavier.youshallnotpass.manager.model

data class EncryptedData(
    val ciphertext: ByteArray,
    val initializationVector: ByteArray
)
