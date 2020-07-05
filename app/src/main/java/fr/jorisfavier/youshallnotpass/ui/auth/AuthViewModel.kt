package fr.jorisfavier.youshallnotpass.ui.auth

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.manager.IAuthManager
import javax.inject.Inject

class AuthViewModel @Inject constructor(private val authManager: IAuthManager) : ViewModel() {
    var authSuccess = MutableLiveData<Boolean>()

    val authCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            authSuccess.value = false
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            authSuccess.value = false
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            authManager.isUserAuthenticated = true
            authSuccess.value = true
        }
    }


}
