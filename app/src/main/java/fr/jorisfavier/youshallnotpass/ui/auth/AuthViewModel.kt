package fr.jorisfavier.youshallnotpass.ui.auth

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel() {
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
            authSuccess.value = true
        }
    }


}
