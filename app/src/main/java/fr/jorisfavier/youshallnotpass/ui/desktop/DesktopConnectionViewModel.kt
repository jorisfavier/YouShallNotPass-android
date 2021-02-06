package fr.jorisfavier.youshallnotpass.ui.desktop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.utils.State
import kotlinx.coroutines.launch
import javax.inject.Inject

class DesktopConnectionViewModel @Inject constructor(private val appPreferences: AppPreferenceDataSource) : ViewModel() {

    private val _qrCodeAnalyseState = MutableLiveData<State>()
    val qrCodeAnalyseState: LiveData<State> = _qrCodeAnalyseState

    fun onCodeFound(code: String?) {
        code?.let { qrCode ->
            _qrCodeAnalyseState.value = State.Loading
            val elements = qrCode.split("#ysnp#")
            val ipRegex = "(\\d+\\.){3}\\d+".toRegex()
            if (elements.size == 2 && elements[0].matches(ipRegex) && elements[1].contains("KEY")) {
                viewModelScope.launch {
                    appPreferences.setDesktopAddress(elements[0])
                    appPreferences.setDesktopPublicKey(cleanUpKey(elements[1]))
                    _qrCodeAnalyseState.value = State.Success
                }
            } else {
                _qrCodeAnalyseState.value = State.Error
            }
        }
    }

    private fun cleanUpKey(key: String): String {
        //Remove the first and last line:
        //-----BEGIN PUBlIC KEY-----
        //-----END PUBLIC KEY-----
        val lines = key.split("\n").toMutableList()
        lines.removeAt(0)
        lines.removeAt(lines.size - 1)
        return lines.joinToString("\n")
    }

}