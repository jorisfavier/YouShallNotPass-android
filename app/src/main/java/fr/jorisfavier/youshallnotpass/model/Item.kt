package fr.jorisfavier.youshallnotpass.model

import androidx.recyclerview.widget.DiffUtil

data class Item(
    val id: Int,
    val title: String,
    val login: String? = null,
    val password: ByteArray,
    val initializationVector: ByteArray,
    val packageCertificate: List<String> = listOf(),
) {
    val hasLogin: Boolean
        get() = !login.isNullOrEmpty()

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Item

        if (id != other.id) return false
        if (title != other.title) return false
        if (login != other.login) return false
        if (!password.contentEquals(other.password)) return false
        if (!initializationVector.contentEquals(other.initializationVector)) return false
        if (packageCertificate != other.packageCertificate) return false
        if (hasLogin != other.hasLogin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + (login?.hashCode() ?: 0)
        result = 31 * result + password.contentHashCode()
        result = 31 * result + initializationVector.contentHashCode()
        result = 31 * result + packageCertificate.hashCode()
        result = 31 * result + hasLogin.hashCode()
        return result
    }
}
