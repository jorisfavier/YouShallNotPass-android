package fr.jorisfavier.youshallnotpass.data.impl

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import fr.jorisfavier.youshallnotpass.analytics.ScreenName
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource.Companion.DESKTOP_ADDRESS_PREFERENCE_KEY
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource.Companion.DESKTOP_KEY_PREFERENCE_KEY
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource.Companion.HIDE_ITEMS_PREFERENCE_KEY
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource.Companion.THEME_PREFERENCE_KEY
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneOffset

class AppPreferenceDataSourceImpl(
    private val prefDataStore: DataStore<Preferences>,
    private val securedSharedPreferences: SharedPreferences,
    private val ioDispatcher: CoroutineDispatcher,
) : AppPreferenceDataSource {

    override val theme: Flow<Int?> = prefDataStore.data.map { preferences ->
        preferences[THEME_PREFERENCE_KEY]?.takeIf { it > 0 }
    }

    override val shouldHideItems: Flow<Boolean> = prefDataStore.data.map { preferences ->
        preferences[HIDE_ITEMS_PREFERENCE_KEY] ?: false
    }

    override suspend fun setTheme(theme: Int) {
        prefDataStore.edit { preferences ->
            preferences[THEME_PREFERENCE_KEY] = theme
        }
    }

    override suspend fun setShouldHideItems(hide: Boolean) {
        prefDataStore.edit { preferences ->
            preferences[HIDE_ITEMS_PREFERENCE_KEY] = hide
        }
    }

    override val desktopAddress: Flow<String?> = prefDataStore.data.map { preferences ->
        preferences[DESKTOP_ADDRESS_PREFERENCE_KEY]?.takeIf { it.isNotEmpty() }
    }

    override suspend fun setDesktopAddress(address: String) {
        prefDataStore.edit { preferences ->
            preferences[DESKTOP_ADDRESS_PREFERENCE_KEY] = address
        }
    }

    override suspend fun setDesktopPublicKey(key: String) {
        withContext(ioDispatcher) {
            securedSharedPreferences.edit(commit = true) {
                putString(
                    DESKTOP_KEY_PREFERENCE_KEY,
                    key
                )
            }
        }
    }

    override suspend fun getDesktopPublicKey(): String? {
        return withContext(ioDispatcher) {
            securedSharedPreferences.getString(DESKTOP_KEY_PREFERENCE_KEY, null)
        }
    }

    override suspend fun setAnalyticEventDate(screenName: ScreenName, date: LocalDateTime) {
        prefDataStore.edit { preferences ->
            preferences[longPreferencesKey(screenName.event)] = date.toEpochSecond(ZoneOffset.UTC)
        }
    }

    override suspend fun getAnalyticEventDate(screenName: ScreenName): LocalDateTime? {
        val time = prefDataStore.data.map { preferences ->
            preferences[longPreferencesKey(screenName.event)]
        }.firstOrNull() ?: 0L
        return if (time != 0L) {
            LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC)
        } else null
    }
}
