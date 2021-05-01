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
}