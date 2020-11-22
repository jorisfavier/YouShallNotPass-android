package fr.jorisfavier.youshallnotpass.di

import android.app.Application
import android.app.KeyguardManager
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import dagger.Module
import dagger.Provides
import fr.jorisfavier.youshallnotpass.YouShallNotPassDatabase
import fr.jorisfavier.youshallnotpass.data.IExternalItemDataSource
import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.data.impl.ExternalItemDataSource
import fr.jorisfavier.youshallnotpass.manager.IAuthManager
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.manager.impl.AuthManager
import fr.jorisfavier.youshallnotpass.manager.impl.CryptoManager
import fr.jorisfavier.youshallnotpass.repository.IExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.repository.impl.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.impl.ItemRepository
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

    @Provides
    fun provideClipboardManager(app: Application): ClipboardManager {
        return app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    @Singleton
    @Provides
    fun provideAuthManager(): IAuthManager {
        return AuthManager()
    }

    @Singleton
    @Provides
    fun provideSharedPreference(app: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app)
    }

    @Singleton
    @Provides
    fun provideExternalItemDataSource(app: Application, contentResolver: ContentResolver): IExternalItemDataSource {
        return ExternalItemDataSource(app.applicationContext, contentResolver)
    }

    @Singleton
    @Provides
    fun provideExternalItemRepository(externalItemDataSource: IExternalItemDataSource, cryptoManager: ICryptoManager): IExternalItemRepository {
        return ExternalItemRepository(externalItemDataSource, cryptoManager)
    }

    @Singleton
    @Provides
    fun provideContentResolver(app: Application): ContentResolver {
        return app.contentResolver
    }

    @Singleton
    @Provides
    fun provideKeyguardManager(app: Application): KeyguardManager {
        return app.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }

}
