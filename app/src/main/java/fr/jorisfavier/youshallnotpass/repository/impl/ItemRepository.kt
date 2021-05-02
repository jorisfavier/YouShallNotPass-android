package fr.jorisfavier.youshallnotpass.repository.impl

import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.repository.mapper.EntityToModel
import fr.jorisfavier.youshallnotpass.repository.mapper.ModelToEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ItemRepository @Inject constructor(private var itemDataSource: ItemDataSource) :
    IItemRepository {

    override suspend fun getAllItems(): List<Item> {
        return withContext(Dispatchers.IO) {
            itemDataSource.getAllItems().map { EntityToModel.itemEntityToItem(it) }
        }
    }

    override suspend fun searchItem(title: String): List<Item> {
        return withContext(Dispatchers.IO) {
            itemDataSource.searchItem(title).map { EntityToModel.itemEntityToItem(it) }
        }
    }

    override suspend fun searchItemByCertificates(certificates: List<String>): List<Item> {
        return withContext(Dispatchers.IO) {
            if (certificates.isEmpty()) {
                listOf()
            } else {
                val certificatesString = Json.encodeToString(certificates)
                itemDataSource.searchItemByCertificate(certificatesString)
                    .map { EntityToModel.itemEntityToItem(it) }
            }
        }
    }

    override suspend fun getItemById(id: Int): Item? {
        return withContext(Dispatchers.IO) {
            itemDataSource.getItemById(id).firstOrNull()?.let {
                EntityToModel.itemEntityToItem(it)
            }
        }
    }

    override suspend fun updateOrCreateItem(item: Item) {
        withContext(Dispatchers.IO) {
            val entity = ModelToEntity.itemToItemEntity(item)
            if (entity.id == 0) {
                itemDataSource.insertItems(entity)
            } else {
                itemDataSource.updateItems(entity)
            }
        }
    }

    override suspend fun deleteItem(item: Item) {
        withContext(Dispatchers.IO) {
            itemDataSource.deleteItems(ModelToEntity.itemToItemEntity(item))
        }
    }

    override suspend fun insertItems(items: List<Item>) {
        withContext(Dispatchers.IO) {
            val entities = items.map { ModelToEntity.itemToItemEntity(it) }.toTypedArray()
            itemDataSource.insertItems(*entities)
        }
    }

    override suspend fun deleteAllItems() {
        withContext(Dispatchers.IO) {
            itemDataSource.deleteAllItems()
        }
    }

}