package fr.jorisfavier.youshallnotpass.ui.auth

import android.app.KeyguardManager
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.manager.IAuthManager
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val authManager: IAuthManager,
    private val keyguardManager: KeyguardManager
) : ViewModel() {

    private var _authSuccess = MutableLiveData<Boolean>()
    var authSuccess: LiveData<Boolean> = _authSuccess

    val authCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            _authSuccess.postValue(false)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            _authSuccess.postValue(false)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            authManager.isUserAuthenticated = true
            _authSuccess.postValue(true)
        }
    }

    fun isDeviceSecure() = keyguardManager.isDeviceSecure


}
