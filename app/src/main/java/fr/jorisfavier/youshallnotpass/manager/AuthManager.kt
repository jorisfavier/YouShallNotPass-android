package fr.jorisfavier.youshallnotpass.manager

interface AuthManager {
    /**
     * Indicates if the user is authenticated in the app
     */
    var isUserAuthenticated: Boolean

    /**
     * Indicates if the autofill service can search for items
     */
    val isAutofillUnlocked: Boolean
}