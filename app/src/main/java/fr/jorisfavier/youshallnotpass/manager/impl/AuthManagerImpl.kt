package fr.jorisfavier.youshallnotpass.manager.impl

import fr.jorisfavier.youshallnotpass.manager.AuthManager
import java.util.*

private const val TEN_MINUTES_MILLI_SEC = 10 * 60 * 1000

class AuthManagerImpl : AuthManager {
    override var isUserAuthenticated: Boolean = false
        set(value) {
            lastAuthentication = Calendar.getInstance().timeInMillis
            field = value
        }
    override val isAutofillUnlocked: Boolean
        get() {
            val current = Calendar.getInstance().timeInMillis
            val diff = current - lastAuthentication
            return diff < TEN_MINUTES_MILLI_SEC

        }
    private var lastAuthentication: Long = 0
}