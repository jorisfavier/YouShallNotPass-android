package fr.jorisfavier.youshallnotpass.model

class Item(
    val id: Int,
    val title: String,
    val login: String? = null,
    val password: ByteArray,
    val initializationVector: ByteArray
) {
    val hasLogin: Boolean
        get() = !login.isNullOrEmpty()
}