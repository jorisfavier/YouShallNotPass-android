package fr.jorisfavier.youshallnotpass

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import fr.jorisfavier.youshallnotpass.dao.ItemDao
import fr.jorisfavier.youshallnotpass.managers.support.FingerPrintAuthManager
import fr.jorisfavier.youshallnotpass.managers.IFingerPrintAuthManager
import fr.jorisfavier.youshallnotpass.managers.IItemManager
import fr.jorisfavier.youshallnotpass.managers.support.ItemManager
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
    fun fingerPrintManagerProvider(): IFingerPrintAuthManager {
        return FingerPrintAuthManager()
    }

    @Singleton
    @Provides
    fun provideItemManager(itemDao: ItemDao): IItemManager {
        return ItemManager(itemDao)
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
    fun provideItemDao(db: YouShallNotPassDatabase): ItemDao {
        return db.itemDao()
    }

}