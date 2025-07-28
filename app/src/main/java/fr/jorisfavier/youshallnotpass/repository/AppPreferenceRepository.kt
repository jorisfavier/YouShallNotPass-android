package fr.jorisfavier.youshallnotpass.repository

import kotlinx.coroutines.flow.Flow

interface AppPreferenceRepository {
    /**
     * Retrieves the theme chosen by the user
     * @return null if not set
     */
    val theme: Flow<Int?>

    /**
     * Set the user's theme
     * @param theme
     */
    suspend fun setTheme(theme: Int): Result<Unit>

    /**
     * Indicates if the we should hide all items on the home screen
     */
    val shouldHideItems: Flow<Boolean>

    /**
     * Set the user's choice regarding hiding the items on the home screen
     */
    suspend fun setShouldHideItems(hide: Boolean): Result<Unit>

}
