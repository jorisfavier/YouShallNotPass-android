package fr.jorisfavier.youshallnotpass

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.data.model.Item

@Database(entities = arrayOf(Item::class), version = 1)
abstract class YouShallNotPassDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDataSource
}
