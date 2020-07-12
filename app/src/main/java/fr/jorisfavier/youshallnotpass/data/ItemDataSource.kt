package fr.jorisfavier.youshallnotpass.data

import androidx.room.*
import fr.jorisfavier.youshallnotpass.data.model.Item


@Dao
interface ItemDataSource {

    /**
     * Returns all stored items
     * @return a list of item
     */
    @Query("SELECT * from Item")
    fun getAllItems(): List<Item>

    /***
     *  Search for an item in the database based on his name
     *  @param title the item's name to search for
     *  @return a list of item
     */
    @Query("SELECT * from Item where title like :title")
    fun searchItem(title: String): List<Item>

    /**
     * Insert an item into the database
     * If the item already exist it will be replaced
     * @param items
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(vararg items: Item)

    /**
     * Returns all the item with the given id
     * @param id an item identifier
     * @return a list of item, if the item hasn't been found it will return an empty list
     */
    @Query("SELECT * from Item where id=:id")
    fun getItemById(id: Int): List<Item>

    /**
     * Update items to the database
     * @param items
     */
    @Update
    fun updateItems(vararg items: Item)

    /**
     * Delete items from the database
     * @param items
     */
    @Delete
    fun deleteItems(vararg items: Item)
}