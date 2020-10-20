package fr.jorisfavier.youshallnotpass

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.data.model.ItemEntity

@Database(entities = [ItemEntity::class], version = 1, exportSchema = false)
abstract class YouShallNotPassDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDataSource
}
