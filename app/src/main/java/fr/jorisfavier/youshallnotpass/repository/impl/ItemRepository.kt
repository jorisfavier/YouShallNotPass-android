package fr.jorisfavier.youshallnotpass.repository.impl

import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.data.model.Item
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ItemRepository @Inject constructor(private var itemDataSource: ItemDataSource) : IItemRepository {

    override suspend fun searchItem(title: String): List<Item> {
        return withContext(Dispatchers.IO) {
            itemDataSource.searchItem(title)
        }
    }

    override suspend fun getItemById(id: Int): Item? {
        return withContext(Dispatchers.IO) {
            itemDataSource.getItemById(id).firstOrNull()
        }
    }

    override suspend fun updateOrCreateItem(item: Item) {
        withContext(Dispatchers.IO) {
            if (item.id == 0) {
                itemDataSource.insertItems(item)
            } else {
                itemDataSource.updateItems(item)
            }
        }
    }

    override suspend fun deleteItem(item: Item) {
        withContext(Dispatchers.IO) {
            itemDataSource.deleteItems(item)
        }
    }


}