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
     * Persist an item into the app
     * @param title the item's name
     * @param password the item's encrypted password
     * @param iv the item's initialization vector
     */
    suspend fun storeItem(title: String, password: ByteArray, iv: ByteArray)

    /**
     * Retrieves an item base on the given id
     * @param id the item's identifier
     * @return an item or null if not found
     */
    suspend fun getItemById(id: Int): Item?
}