package fr.jorisfavier.youshallnotpass.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.jorisfavier.youshallnotpass.data.model.Item


@Dao
interface ItemDataSource {

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
}