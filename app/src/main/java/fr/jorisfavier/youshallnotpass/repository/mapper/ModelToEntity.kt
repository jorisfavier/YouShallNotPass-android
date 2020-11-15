package fr.jorisfavier.youshallnotpass.repository.mapper

import fr.jorisfavier.youshallnotpass.data.model.ItemEntity
import fr.jorisfavier.youshallnotpass.model.Item

object ModelToEntity {
    fun itemToItemEntity(item: Item): ItemEntity {
        return ItemEntity(item.id, item.title, item.password, item.initializationVector)
    }
}