package fr.jorisfavier.youshallnotpass.model

class Item(
    val id: Int,
    val title: String,
    val password: ByteArray,
    val initializationVector: ByteArray
)