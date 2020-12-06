package fr.jorisfavier.youshallnotpass.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.manager.IAuthManager
import timber.log.Timber
import javax.inject.Inject

class HomeViewModel @Inject constructor(private val authManager: IAuthManager) : ViewModel() {

    //indicates that the user didn't put the app in background
    //but the app started an intent
    private var ignoreNextPause = false
    private val _requireAuthentication = MutableLiveData<Unit>()
    val requireAuthentication: LiveData<Unit> = _requireAuthentication

    fun onConfigurationChanged() {
        authManager.isUserAuthenticated = true
    }

    fun onAppPaused() {
        if (!ignoreNextPause) {
            authManager.isUserAuthenticated = false
        }
        ignoreNextPause = false
    }

    fun onAppResumed() {
        if (!authManager.isUserAuthenticated) {
            _requireAuthentication.value = Unit
        }
    }

    fun ignoreNextPause() {
        Timber.d("Ignore next pause")
        ignoreNextPause = true
    }


}