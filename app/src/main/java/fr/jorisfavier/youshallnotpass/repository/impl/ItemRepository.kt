package fr.jorisfavier.youshallnotpass.repository.impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.data.models.Item
import javax.inject.Inject

class ItemRepository @Inject constructor(private var itemDataSource: ItemDataSource) : IItemRepository {

    //TODO implement this method by using the dao when the creation method will be ready
    override fun searchItem(title: String): LiveData<List<Item>> {
        val result = MutableLiveData<List<Item>>()
        result.value = listOf(Item(1,"Vente privée"), Item(2, "Canal+"))
        return result
    }


}