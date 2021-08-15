package fr.jorisfavier.youshallnotpass.repository.mapper

import fr.jorisfavier.youshallnotpass.data.model.ItemEntity
import fr.jorisfavier.youshallnotpass.model.Item
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ModelToEntity {
    fun itemToItemEntity(item: Item): ItemEntity {
        return ItemEntity(
            id = item.id,
            title = item.title,
            login = item.login,
            password = item.password,
            initializationVector = item.initializationVector,
            packageCertificates = Json.encodeToString(item.packageCertificate),
        )
    }
}