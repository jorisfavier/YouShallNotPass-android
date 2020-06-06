package fr.jorisfavier.youshallnotpass.repository

import androidx.lifecycle.LiveData
import fr.jorisfavier.youshallnotpass.data.models.Item

interface IItemRepository {

    /***
     *  Search for an item based on his title
     *  @param title the item's name to search for
     *  @return a list of item
     */
    fun searchItem(title: String): LiveData<List<Item>>
}