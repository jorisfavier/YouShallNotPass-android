package fr.jorisfavier.youshallnotpass.data

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
    suspend fun getShouldHideItems(): Boolean

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
}