package fr.jorisfavier.youshallnotpass.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import fr.jorisfavier.youshallnotpass.YSNPApplication
import fr.jorisfavier.youshallnotpass.YouShallNotPassDatabase
import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.manager.impl.CryptoManager
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.repository.impl.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.search.SearchFragment
import javax.inject.Singleton


@Module(includes = [ViewModelModule::class])
class AppModule {

    @Singleton
    @Provides
    fun provideItemRepository(itemDataSource: ItemDataSource): IItemRepository {
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
    fun provideItemDataSource(db: YouShallNotPassDatabase): ItemDataSource {
        return db.itemDao()
    }

    @Singleton
    @Provides
    fun provideCryptoManager(): ICryptoManager {
        return CryptoManager()
    }
}
