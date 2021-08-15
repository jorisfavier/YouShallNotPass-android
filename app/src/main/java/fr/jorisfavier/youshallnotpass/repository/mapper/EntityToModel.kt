package fr.jorisfavier.youshallnotpass.repository.mapper

import fr.jorisfavier.youshallnotpass.data.model.ItemEntity
import fr.jorisfavier.youshallnotpass.model.Item
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object EntityToModel {
    fun itemEntityToItem(entity: ItemEntity): Item {
        return Item(
            id = entity.id,
            title = entity.title,
            login = entity.login,
            password = entity.password,
            initializationVector = entity.initializationVector,
            packageCertificate = entity.packageCertificates?.let { Json.decodeFromString(it) }
                ?: emptyList(),
        )
    }
}