package fr.jorisfavier.youshallnotpass.repository

import fr.jorisfavier.youshallnotpass.model.Item

interface IItemRepository {

    /**
     * Return all items stored in the app
     * @return a list of item
     */
    suspend fun getAllItems(): List<Item>

    /***
     *  Search for an item based on his title
     *  @param title the item's name to search for
     *  @return a list of item
     */
    suspend fun searchItem(title: String): List<Item>

    /***
     *  Search for an item based on his certificates
     *  @param certificates a list of certificate associated with an Item
     *  @return a list of item
     */
    suspend fun searchItemByCertificates(certificates: List<String>): List<Item>

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

    /**
     * Delete an item from the app
     * @param item
     */
    suspend fun deleteItem(item: Item)

    /**
     * Persist a list of item into the app
     * @param items
     */
    suspend fun insertItems(items: List<Item>)

    /**
     * Delete all the items from the app
     */
    suspend fun deleteAllItems()
}