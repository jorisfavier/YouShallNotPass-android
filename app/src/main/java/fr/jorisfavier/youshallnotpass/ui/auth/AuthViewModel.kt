package fr.jorisfavier.youshallnotpass.ui.auth

import android.app.KeyguardManager
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.AuthManager
import fr.jorisfavier.youshallnotpass.utils.Event
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val keyguardManager: KeyguardManager,
    private val biometricManager: BiometricManager,
) : ViewModel() {

    sealed class AuthStatus {
        object Success : AuthStatus()
        object NonSecure : AuthStatus()
        object NoBiometric : AuthStatus()
        object SetupBiometric : AuthStatus()
        object Ready : AuthStatus()
        class Failure(@StringRes val errorMessage: Int) : AuthStatus()
    }

    private var _authStatus = MutableLiveData<Event<AuthStatus>>()
    var authStatus: LiveData<Event<AuthStatus>> = _authStatus

    val authCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            val message = if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                R.string.auth_fail_no_biometrics
            } else {
                R.string.auth_fail_try_again
            }
            _authStatus.value = Event(AuthStatus.Failure(message))
            Timber.e("Error during authentication: $errorCode - $errString")
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            _authStatus.value = Event(AuthStatus.Failure(R.string.auth_fail_try_again))
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            authManager.isUserAuthenticated = true
            _authStatus.value = Event(AuthStatus.Success)
        }
    }

    fun requestAuthentication() {
        _authStatus.value = if (keyguardManager.isDeviceSecure) {
            when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)) {
                BiometricManager.BIOMETRIC_SUCCESS -> Event(AuthStatus.Ready)
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Event(AuthStatus.SetupBiometric)
                else -> Event(AuthStatus.NoBiometric)
            }
        } else {
            Event(AuthStatus.NonSecure)
        }
    }
}
