package fr.jorisfavier.youshallnotpass.data

import androidx.room.Dao
import androidx.room.Query
import fr.jorisfavier.youshallnotpass.data.models.Item

@Dao
interface ItemDataSource {

    /***
     *  Search for an item in the database based on his name
     *  @param title the item's name to search for
     *  @return a list of item
     */
    @Query("SELECT * from Item where title like :title")
    fun searchItem(title: String): List<Item>
}