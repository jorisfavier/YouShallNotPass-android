package fr.jorisfavier.youshallnotpass.data

import fr.jorisfavier.youshallnotpass.analytics.ScreenName
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface AppPreferenceDataSource {
    /**
     * Retrieves the theme chosen by the user
     * @return null if not set
     */
    suspend fun getTheme(): String?

    /**
     * Set the user's theme
     * @param theme
     */
    suspend fun setTheme(theme: String)

    /**
     * Indicates if the we should hide all items on the home screen
     */
    fun observeShouldHideItems(): Flow<Boolean>

    /**
     * Set the user's choice regarding hiding the items on the home screen
     */
    suspend fun setShouldHideItems(hide: Boolean)

    /**
     * Retrieves the ip address from the desktop ysnp app
     * @return null if unknown
     */
    suspend fun getDesktopAddress(): String?

    /**
     * Set the ip address from the desktop ysnp app
     * @param address the ip address
     */
    suspend fun setDesktopAddress(address: String)

    /**
     * Set the public key from the desktop ysnp app
     * @param key the key
     */
    suspend fun setDesktopPublicKey(key: String)

    /**
     * Retrieves the public key from the desktop ysnp app
     * @return null if unknown
     */
    suspend fun getDesktopPublicKey(): String?

    /**
     * Save a date for a given screenName
     * @param screenName the analytic screen name
     * @param date the date when the event was sent
     */
    suspend fun setAnalyticEventDate(screenName: ScreenName, date: LocalDateTime)

    /**
     * Retrieve the date for an analytic screen name
     * @return the date when the event was sent or null if not found
     */
    suspend fun getAnalyticEventDate(screenName: ScreenName): LocalDateTime?

    companion object {
        const val THEME_PREFERENCE_KEY = "theme"
        const val HIDE_ITEMS_PREFERENCE_KEY = "hideItems"
        const val DESKTOP_ADDRESS_PREFERENCE_KEY = "desktop_address_preference_key"
        const val DESKTOP_KEY_PREFERENCE_KEY = "desktop_key_preference_key"
    }
}