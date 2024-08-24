package fr.jorisfavier.youshallnotpass.repository.impl

import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.repository.mapper.EntityToModel
import fr.jorisfavier.youshallnotpass.repository.mapper.ModelToEntity
import fr.jorisfavier.youshallnotpass.utils.extensions.suspendRunCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private var itemDataSource: ItemDataSource,
) : ItemRepository {

    override fun getAllItems(): Flow<List<Item>> {
        return itemDataSource.getAllItems().map { items ->
            items.map(EntityToModel::itemEntityToItem)
        }
    }

    override suspend fun searchItem(title: String): Result<List<Item>> {
        return suspendRunCatching(
            errorMessage = "An error occurred while searching items with name: $title",
        ) {
            itemDataSource.searchItem(title).map { EntityToModel.itemEntityToItem(it) }
        }
    }

    override suspend fun searchItemByCertificates(certificates: List<String>): Result<List<Item>> {
        return suspendRunCatching(
            errorMessage = "An error occurred while searching items by certificates",
        ) {
            if (certificates.isEmpty()) {
                listOf()
            } else {
                val certificatesString = Json.encodeToString(certificates)
                itemDataSource.searchItemByCertificate(certificatesString)
                    .map { EntityToModel.itemEntityToItem(it) }
            }
        }
    }

    override suspend fun getItemById(id: Int): Result<Item?> {
        return suspendRunCatching(
            errorMessage = "An error occurred while retrieving an item with id: $id",
        ) {
            itemDataSource.getItemById(id).firstOrNull()?.let {
                EntityToModel.itemEntityToItem(it)
            }
        }
    }

    override suspend fun updateOrCreateItem(item: Item): Result<Unit> {
        return suspendRunCatching(
            errorMessage = "An error occurred while updating or creating an item with id: ${item.id}",
        ) {
            val entity = ModelToEntity.itemToItemEntity(item)
            if (entity.id == 0) {
                itemDataSource.insertItems(entity)
            } else {
                itemDataSource.updateItems(entity)
            }
        }
    }

    override suspend fun deleteItem(item: Item): Result<Unit> {
        return suspendRunCatching(
            errorMessage = "An error occurred while deleting an item with id: ${item.id}",
        ) {
            itemDataSource.deleteItems(ModelToEntity.itemToItemEntity(item))
        }
    }

    override suspend fun insertItems(items: List<Item>): Result<Unit> {
        return suspendRunCatching(
            errorMessage = "An error occurred while inserting items",
        ) {
            val entities = items.map { ModelToEntity.itemToItemEntity(it) }.toTypedArray()
            itemDataSource.insertItems(*entities)
        }
    }

    override suspend fun deleteAllItems(): Result<Unit> {
        return suspendRunCatching(
            errorMessage = "An error occurred while deleting all the items",
        ) {
            itemDataSource.deleteAllItems()
        }
    }

}