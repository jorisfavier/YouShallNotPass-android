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

    override suspend fun storeItem(title: String, password: ByteArray, iv: ByteArray) {
        withContext(Dispatchers.IO) {
            itemDataSource.insertItems(Item(0, title, password, iv))
        }
    }

    override suspend fun getItemById(id: Int): Item? {
        return withContext(Dispatchers.IO) {
            itemDataSource.getItemById(id).firstOrNull()
        }
    }


}