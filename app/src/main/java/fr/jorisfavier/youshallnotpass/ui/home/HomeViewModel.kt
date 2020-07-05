package fr.jorisfavier.youshallnotpass.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.manager.IAuthManager
import javax.inject.Inject

class HomeViewModel @Inject constructor(private val authManager: IAuthManager) : ViewModel() {

    private val _requireAuthentication = MutableLiveData<Unit>()
    val requireAuthentication: LiveData<Unit> = _requireAuthentication


    fun onAppPaused() {
        authManager.isUserAuthenticated = false
    }

    fun onAppResumed() {
        if (!authManager.isUserAuthenticated) {
            _requireAuthentication.value = Unit
        }
    }


}