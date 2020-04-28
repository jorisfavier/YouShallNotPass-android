package fr.jorisfavier.youshallnotpass.managers

import androidx.lifecycle.LiveData
import fr.jorisfavier.youshallnotpass.models.Item

interface IItemManager {

    /***
     *  Search for an item based on his title
     *  @param title the item's name to search for
     *  @return a list of item
     */
    fun searchItem(title: String): LiveData<List<Item>>
}