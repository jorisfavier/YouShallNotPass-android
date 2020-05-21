package fr.jorisfavier.youshallnotpass

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.repository.impl.ItemRepository
import javax.inject.Singleton


@Module
class AppModule(val application: YSNPApplication) {

    @Singleton
    @Provides
    fun applicationProvider(): Application {
        return application
    }

    @Singleton
    @Provides
    fun provideItemManager(itemDataSource: ItemDataSource): IItemRepository {
        return ItemRepository(itemDataSource)
    }

    @Singleton
    @Provides
    fun provideDb(app: Application): YouShallNotPassDatabase {
        return Room.databaseBuilder(app, YouShallNotPassDatabase::class.java, "ysnp.db")
                .fallbackToDestructiveMigration()
                .build()
    }

    @Singleton
    @Provides
    fun provideItemDao(db: YouShallNotPassDatabase): ItemDataSource {
        return db.itemDao()
    }

}
