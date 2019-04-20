package fr.jorisfavier.youshallnotpass.managers.support

import fr.jorisfavier.youshallnotpass.dao.ItemDao
import fr.jorisfavier.youshallnotpass.managers.IItemManager
import fr.jorisfavier.youshallnotpass.models.Item
import javax.inject.Inject

class ItemManager @Inject constructor(private var itemDao: ItemDao) : IItemManager {

    override fun searchItem(title: String): List<Item> {
        return listOf(Item(1,"Vente privée"), Item(2, "Canal+"))
    }


}