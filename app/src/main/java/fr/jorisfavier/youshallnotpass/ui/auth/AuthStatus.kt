package fr.jorisfavier.youshallnotpass.ui.auth

import androidx.annotation.StringRes

sealed interface AuthStatus {
    data object Success : AuthStatus
    data object NonSecure : AuthStatus
    data object NoBiometric : AuthStatus
    data object SetupBiometric : AuthStatus
    data object Ready : AuthStatus
    data class Failure(@StringRes val errorMessage: Int) : AuthStatus
}
