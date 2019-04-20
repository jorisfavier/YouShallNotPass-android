package fr.jorisfavier.youshallnotpass

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.jorisfavier.youshallnotpass.dao.ItemDao
import fr.jorisfavier.youshallnotpass.models.Item

@Database(entities = arrayOf(Item::class), version = 1)
abstract class YouShallNotPassDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
