package fr.jorisfavier.youshallnotpass.dao

import androidx.room.Dao
import androidx.room.Query
import fr.jorisfavier.youshallnotpass.models.Item

@Dao
interface ItemDao {
    @Query("SELECT * from Item where title like :title")
    fun searchItem(title: String): List<Item>
}