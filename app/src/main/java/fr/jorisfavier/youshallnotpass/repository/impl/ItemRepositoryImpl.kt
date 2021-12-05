package fr.jorisfavier.youshallnotpass.repository.impl

import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.repository.mapper.EntityToModel
import fr.jorisfavier.youshallnotpass.repository.mapper.ModelToEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(private var itemDataSource: ItemDataSource) :
    ItemRepository {

    override fun getAllItems(): Flow<List<Item>> {
        return itemDataSource.getAllItems().map { items ->
            items.map(EntityToModel::itemEntityToItem)
        }
    }

    override suspend fun searchItem(title: String): List<Item> {
        return itemDataSource.searchItem(title).map { EntityToModel.itemEntityToItem(it) }
    }

    override suspend fun searchItemByCertificates(certificates: List<String>): List<Item> {
        return if (certificates.isEmpty()) {
            listOf()
        } else {
            val certificatesString = Json.encodeToString(certificates)
            itemDataSource.searchItemByCertificate(certificatesString)
                .map { EntityToModel.itemEntityToItem(it) }
        }
    }

    override suspend fun getItemById(id: Int): Item? {
        return itemDataSource.getItemById(id).firstOrNull()?.let {
            EntityToModel.itemEntityToItem(it)
        }
    }

    override suspend fun updateOrCreateItem(item: Item) {
        val entity = ModelToEntity.itemToItemEntity(item)
        if (entity.id == 0) {
            itemDataSource.insertItems(entity)
        } else {
            itemDataSource.updateItems(entity)
        }
    }

    override suspend fun deleteItem(item: Item) {
        itemDataSource.deleteItems(ModelToEntity.itemToItemEntity(item))
    }

    override suspend fun insertItems(items: List<Item>) {
        val entities = items.map { ModelToEntity.itemToItemEntity(it) }.toTypedArray()
        itemDataSource.insertItems(*entities)
    }

    override suspend fun deleteAllItems() {
        itemDataSource.deleteAllItems()
    }

}