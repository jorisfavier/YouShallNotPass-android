package fr.jorisfavier.youshallnotpass.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    val title: String?,
    val login: String?,
    val password: String?,
)