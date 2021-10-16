package fr.jorisfavier.youshallnotpass.ui.auth

import android.app.KeyguardManager
import androidx.annotation.StringRes
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
    private val keyguardManager: KeyguardManager
) : ViewModel() {

    sealed class AuthResult {
        object Success : AuthResult()
        class Failure(@StringRes val errorMessage: Int) : AuthResult()
    }

    private var _authSuccess = MutableLiveData<Event<AuthResult>>()
    var authSuccess: LiveData<Event<AuthResult>> = _authSuccess

    val authCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            val message = if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                R.string.auth_fail_no_biometrics
            } else {
                R.string.auth_fail_try_again
            }
            _authSuccess.postValue(Event(AuthResult.Failure(message)))
            Timber.e("Error during authentication: $errorCode - $errString")
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            _authSuccess.postValue(Event(AuthResult.Failure(R.string.auth_fail_try_again)))
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            authManager.isUserAuthenticated = true
            _authSuccess.postValue(Event(AuthResult.Success))
        }
    }

    fun isDeviceSecure() = keyguardManager.isDeviceSecure


}
