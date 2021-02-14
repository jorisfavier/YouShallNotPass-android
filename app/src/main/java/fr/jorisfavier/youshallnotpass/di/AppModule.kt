package fr.jorisfavier.youshallnotpass.di

import android.app.Application
import android.app.KeyguardManager
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import fr.jorisfavier.youshallnotpass.BuildConfig
import fr.jorisfavier.youshallnotpass.YouShallNotPassDatabase
import fr.jorisfavier.youshallnotpass.api.DesktopApi
import fr.jorisfavier.youshallnotpass.api.HostInterceptor
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.data.ExternalItemDataSource
import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.data.impl.AppPreferenceDataSourceImpl
import fr.jorisfavier.youshallnotpass.data.impl.ExternalItemDataSourceImpl
import fr.jorisfavier.youshallnotpass.manager.IAuthManager
import fr.jorisfavier.youshallnotpass.manager.IContentResolverManager
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.manager.impl.AuthManager
import fr.jorisfavier.youshallnotpass.manager.impl.ContentResolverManager
import fr.jorisfavier.youshallnotpass.manager.impl.CryptoManager
import fr.jorisfavier.youshallnotpass.repository.DesktopRepository
import fr.jorisfavier.youshallnotpass.repository.IExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.repository.impl.DesktopRepositoryImpl
import fr.jorisfavier.youshallnotpass.repository.impl.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.impl.ItemRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named
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
    @Named("SecuredSharedPreferences")
    fun provideSecureSharedPreference(app: Application): SharedPreferences {
        val masterKey = MasterKey.Builder(app, "storage-master-key")
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            app,
            "secure_preferences.pref",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Singleton
    @Provides
    fun provideAppDataSource(
        sharedPreferences: SharedPreferences,
        @Named("SecuredSharedPreferences")
        securedSharedPreferences: SharedPreferences
    ): AppPreferenceDataSource {
        return AppPreferenceDataSourceImpl(sharedPreferences, securedSharedPreferences)
    }

    @Singleton
    @Provides
    fun provideExternalItemDataSource(
        app: Application,
        contentResolver: IContentResolverManager
    ): ExternalItemDataSource {
        return ExternalItemDataSourceImpl(app.applicationContext, contentResolver)
    }

    @Singleton
    @Provides
    fun provideExternalItemRepository(
        externalItemDataSource: ExternalItemDataSource,
        cryptoManager: ICryptoManager
    ): IExternalItemRepository {
        return ExternalItemRepository(externalItemDataSource, cryptoManager)
    }

    @Singleton
    @Provides
    fun provideContentResolver(app: Application): IContentResolverManager {
        return ContentResolverManager(app.contentResolver)
    }

    @Singleton
    @Provides
    fun provideKeyguardManager(app: Application): KeyguardManager {
        return app.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }

    @Singleton
    @Provides
    fun provideHostInterceptor() = HostInterceptor()

    @Singleton
    @Provides
    fun provideHttpClient(hostInterceptor: HostInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(hostInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(httpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl("http://0.0.0.0")
            .build()
    }

    @Singleton
    @Provides
    fun provideDesktopApi(retrofit: Retrofit): DesktopApi {
        return retrofit.create(DesktopApi::class.java)
    }

    @Singleton
    @Provides
    fun provideDesktopRepository(
        api: DesktopApi,
        appPreferenceDataSource: AppPreferenceDataSource,
        hostInterceptor: HostInterceptor,
        cryptoManager: ICryptoManager
    ): DesktopRepository {
        GlobalScope.launch {
            hostInterceptor.host = appPreferenceDataSource.getDesktopAddress()
        }
        return DesktopRepositoryImpl(api, appPreferenceDataSource, hostInterceptor, cryptoManager)
    }


}
