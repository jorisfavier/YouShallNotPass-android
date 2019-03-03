package fr.jorisfavier.youshallnotpass.features.home

import android.content.Context
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.managers.IFingerPrintAuthManager

class HomeViewModel : ViewModel() {
    lateinit var authManager: IFingerPrintAuthManager
    var authSuccess = MutableLiveData<Boolean>()


    fun requestAuth(baseContext: Context){
        authManager.fingerPrintAuth(baseContext, object: FingerprintManagerCompat.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                authSuccess.value = true
            }

            override fun onAuthenticationFailed() {
                authSuccess.value = false
            }

            override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                authSuccess.value = false
            }
        })
    }


}