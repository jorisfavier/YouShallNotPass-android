package fr.jorisfavier.youshallnotpass.data.impl

import android.content.SharedPreferences
import androidx.core.content.edit
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource.Companion.DESKTOP_ADDRESS_PREFERENCE_KEY
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource.Companion.DESKTOP_KEY_PREFERENCE_KEY
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource.Companion.HIDE_ITEMS_PREFERENCE_KEY
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource.Companion.THEME_PREFERENCE_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.withContext

class AppPreferenceDataSourceImpl(
    private val sharedPreferences: SharedPreferences,
    private val securedSharedPreferences: SharedPreferences
) : AppPreferenceDataSource {

    private val _shouldHideItems = MutableStateFlow(false)

    init {
        val onPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                if (key == HIDE_ITEMS_PREFERENCE_KEY) {
                    _shouldHideItems.value =
                        sharedPreferences.getBoolean(HIDE_ITEMS_PREFERENCE_KEY, false)
                }
            }
        sharedPreferences.registerOnSharedPreferenceChangeListener(onPreferenceChangeListener)
    }

    override suspend fun getTheme(): String? {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString(THEME_PREFERENCE_KEY, null)
        }
    }

    override suspend fun setTheme(theme: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit(commit = true) { putString(THEME_PREFERENCE_KEY, theme) }
        }
    }

    override suspend fun getShouldHideItems(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(HIDE_ITEMS_PREFERENCE_KEY, false)
        }
    }

    override fun observeShouldHideItems(): Flow<Boolean> {
        return _shouldHideItems.onSubscription {
            withContext(Dispatchers.IO) {
                _shouldHideItems.value =
                    sharedPreferences.getBoolean(HIDE_ITEMS_PREFERENCE_KEY, false)
            }
        }
    }

    override suspend fun setShouldHideItems(hide: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit(commit = true) { putBoolean(HIDE_ITEMS_PREFERENCE_KEY, hide) }
            _shouldHideItems.value = hide
        }
    }

    override suspend fun getDesktopAddress(): String? {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString(DESKTOP_ADDRESS_PREFERENCE_KEY, null)
        }
    }

    override suspend fun setDesktopAddress(address: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit(commit = true) {
                putString(
                    DESKTOP_ADDRESS_PREFERENCE_KEY,
                    address
                )
            }
        }
    }

    override suspend fun setDesktopPublicKey(key: String) {
        withContext(Dispatchers.IO) {
            securedSharedPreferences.edit(commit = true) {
                putString(
                    DESKTOP_KEY_PREFERENCE_KEY,
                    key
                )
            }
        }
    }

    override suspend fun getDesktopPublicKey(): String? {
        return withContext(Dispatchers.IO) {
            securedSharedPreferences.getString(DESKTOP_KEY_PREFERENCE_KEY, null)
        }
    }

}