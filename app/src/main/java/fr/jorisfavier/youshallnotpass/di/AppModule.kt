package fr.jorisfavier.youshallnotpass.di

import android.app.Application
import android.app.KeyguardManager
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.biometric.BiometricManager
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.jorisfavier.youshallnotpass.BuildConfig
import fr.jorisfavier.youshallnotpass.YouShallNotPassDatabase
import fr.jorisfavier.youshallnotpass.analytics.YSNPAnalytics
import fr.jorisfavier.youshallnotpass.analytics.impl.YSNPAnalyticsImpl
import fr.jorisfavier.youshallnotpass.api.AnalyticsApi
import fr.jorisfavier.youshallnotpass.api.AuthInterceptor
import fr.jorisfavier.youshallnotpass.api.DesktopApi
import fr.jorisfavier.youshallnotpass.api.HostInterceptor
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.data.ExternalItemDataSource
import fr.jorisfavier.youshallnotpass.data.ItemDataSource
import fr.jorisfavier.youshallnotpass.data.impl.AppPreferenceDataSourceImpl
import fr.jorisfavier.youshallnotpass.data.impl.ExternalItemDataSourceImpl
import fr.jorisfavier.youshallnotpass.manager.ContentResolverManager
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.manager.impl.ContentResolverManagerImpl
import fr.jorisfavier.youshallnotpass.manager.impl.CryptoManagerImpl
import fr.jorisfavier.youshallnotpass.repository.DesktopRepository
import fr.jorisfavier.youshallnotpass.repository.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.repository.impl.DesktopRepositoryImpl
import fr.jorisfavier.youshallnotpass.repository.impl.ExternalItemRepositoryImpl
import fr.jorisfavier.youshallnotpass.repository.impl.ItemRepositoryImpl
import fr.jorisfavier.youshallnotpass.utils.CoroutineDispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named
import javax.inject.Singleton

private const val ANALYTICS_HTTP_CLIENT = "analytics_http_client"
private const val ANALYTICS_RETROFIT = "analytics_retrofit"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideItemRepository(itemDataSource: ItemDataSource): ItemRepository {
        return ItemRepositoryImpl(itemDataSource)
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
    fun provideClipboardManager(app: Application): ClipboardManager {
        return app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
        securedSharedPreferences: SharedPreferences,
        coroutineDispatcher: CoroutineDispatchers,
    ): AppPreferenceDataSource {
        return AppPreferenceDataSourceImpl(
            sharedPreferences,
            securedSharedPreferences,
            coroutineDispatcher.io,
        )
    }

    @Singleton
    @Provides
    fun provideExternalItemDataSource(
        app: Application,
        contentResolver: ContentResolverManager,
        coroutineDispatcher: CoroutineDispatchers,
    ): ExternalItemDataSource {
        return ExternalItemDataSourceImpl(
            app.applicationContext,
            contentResolver,
            coroutineDispatcher.io,
        )
    }

    @Singleton
    @Provides
    fun provideExternalItemRepository(
        externalItemDataSource: ExternalItemDataSource,
        cryptoManager: CryptoManager,
    ): ExternalItemRepository {
        return ExternalItemRepositoryImpl(
            externalItemDataSource,
            cryptoManager,
        )
    }

    @Singleton
    @Provides
    fun provideContentResolver(
        app: Application,
        coroutineDispatcher: CoroutineDispatchers,
    ): ContentResolverManager {
        return ContentResolverManagerImpl(
            app.contentResolver,
            coroutineDispatcher.io,
        )
    }

    @Singleton
    @Provides
    fun provideKeyguardManager(app: Application): KeyguardManager {
        return app.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }

    @Singleton
    @Provides
    fun provideBiometricManager(app: Application): BiometricManager {
        return BiometricManager.from(app)
    }

    @Singleton
    @Provides
    fun provideCryptoManager(
        coroutineDispatcher: CoroutineDispatchers,
    ): CryptoManager {
        return CryptoManagerImpl(
            ioDispatcher = coroutineDispatcher.io,
        )
    }

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
    @Named(ANALYTICS_HTTP_CLIENT)
    @Provides
    fun provideAnalyticsHttpClient(
        httpClient: OkHttpClient,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        return httpClient
            .newBuilder()
            .addInterceptor(authInterceptor)
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
    @Named(ANALYTICS_RETROFIT)
    @Provides
    fun provideAnalyticsRetrofit(@Named(ANALYTICS_HTTP_CLIENT) httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(httpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(BuildConfig.ANALYTIC_URL)
            .build()
    }

    @Singleton
    @Provides
    fun provideDesktopApi(retrofit: Retrofit): DesktopApi {
        return retrofit.create(DesktopApi::class.java)
    }

    @Singleton
    @Provides
    fun provideAnalyticsApi(@Named(ANALYTICS_RETROFIT) retrofit: Retrofit): AnalyticsApi {
        return retrofit.create(AnalyticsApi::class.java)
    }

    @Singleton
    @Provides
    fun provideDesktopRepository(
        api: DesktopApi,
        appPreferenceDataSource: AppPreferenceDataSource,
        hostInterceptor: HostInterceptor,
        cryptoManager: CryptoManager,
    ): DesktopRepository {
        GlobalScope.launch {
            hostInterceptor.host = appPreferenceDataSource.getDesktopAddress()
        }
        return DesktopRepositoryImpl(api, appPreferenceDataSource, hostInterceptor, cryptoManager)
    }

    @Singleton
    @Provides
    fun providePackageManager(app: Application): PackageManager {
        return app.packageManager
    }

    @Singleton
    @Provides
    fun provideAnalytics(
        analyticsApi: AnalyticsApi,
        appPreferenceDataSource: AppPreferenceDataSource,
    ): YSNPAnalytics {
        return YSNPAnalyticsImpl(
            api = analyticsApi,
            appPreferenceDataSource = appPreferenceDataSource,
        )
    }

    @Singleton
    @Provides
    fun provideCoroutineDispatchers(): CoroutineDispatchers {
        return CoroutineDispatchers()
    }
}
