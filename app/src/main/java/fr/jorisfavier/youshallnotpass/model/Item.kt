package fr.jorisfavier.youshallnotpass.model

import java.io.Serializable

class Item(
    val id: Int,
    val title: String,
    val password: ByteArray,
    val initializationVector: ByteArray
) : Serializable