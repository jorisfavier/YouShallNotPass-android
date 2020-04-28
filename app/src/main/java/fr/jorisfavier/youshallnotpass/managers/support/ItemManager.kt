package fr.jorisfavier.youshallnotpass.managers.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.jorisfavier.youshallnotpass.dao.ItemDao
import fr.jorisfavier.youshallnotpass.managers.IItemManager
import fr.jorisfavier.youshallnotpass.models.Item
import javax.inject.Inject

class ItemManager @Inject constructor(private var itemDao: ItemDao) : IItemManager {

    //TODO implement this method by using the dao when the creation method will be ready
    override fun searchItem(title: String): LiveData<List<Item>> {
        val result = MutableLiveData<List<Item>>()
        result.value = listOf(Item(1,"Vente privée"), Item(2, "Canal+"))
        return result
    }


}