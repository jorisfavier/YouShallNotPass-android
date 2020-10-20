package fr.jorisfavier.youshallnotpass.repository.impl

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.repository.mapper.EntityToModel
import fr.jorisfavier.youshallnotpass.repository.mapper.ModelToEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

class ItemRepository @Inject constructor(private var itemDataSource: ItemDataSource) : IItemRepository {

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

    override suspend fun getItemById(id: Int): Item? {
        return withContext(Dispatchers.IO) {
            itemDataSource.getItemById(id).firstOrNull()?.let {
                EntityToModel.itemEntityToItem(it)
            }
        }
    }

    override suspend fun updateOrCreateItem(item: Item) {
        withContext(Dispatchers.IO) {
            val entity = ModelToEntity.ItemToItemEntity(item)
            if (entity.id == 0) {
                itemDataSource.insertItems(entity)
            } else {
                itemDataSource.updateItems(entity)
            }
        }
    }

    override suspend fun deleteItem(item: Item) {
        withContext(Dispatchers.IO) {
            itemDataSource.deleteItems(ModelToEntity.ItemToItemEntity(item))
        }
    }

}