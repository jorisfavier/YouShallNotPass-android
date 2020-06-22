package fr.jorisfavier.youshallnotpass.repository

import fr.jorisfavier.youshallnotpass.data.model.Item

interface IItemRepository {

    /***
     *  Search for an item based on his title
     *  @param title the item's name to search for
     *  @return a list of item
     */
    suspend fun searchItem(title: String): List<Item>

    /**
     * Retrieves an item base on the given id
     * @param id the item's identifier
     * @return an item or null if not found
     */
    suspend fun getItemById(id: Int): Item?

    /**
     * Persist an item into the app
     * Updates or creates an item
     * @param item
     */
    suspend fun updateOrCreateItem(item: Item)
}