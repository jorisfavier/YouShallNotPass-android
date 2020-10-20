package fr.jorisfavier.youshallnotpass.repository.mapper

import fr.jorisfavier.youshallnotpass.data.model.ItemEntity
import fr.jorisfavier.youshallnotpass.model.Item

object EntityToModel {
    fun itemEntityToItem(entity: ItemEntity): Item {
        return Item(entity.id, entity.title, entity.password, entity.initializationVector)
    }
}