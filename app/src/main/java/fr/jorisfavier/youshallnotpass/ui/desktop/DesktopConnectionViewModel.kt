package fr.jorisfavier.youshallnotpass.ui.desktop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.jorisfavier.youshallnotpass.repository.DesktopRepository
import fr.jorisfavier.youshallnotpass.utils.State
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DesktopConnectionViewModel @Inject constructor(private val desktopRepository: DesktopRepository) :
    ViewModel() {

    private val _qrCodeAnalyseState = MutableLiveData<State<Unit>>()
    val qrCodeAnalyseState: LiveData<State<Unit>> = _qrCodeAnalyseState

    fun onCodeFound(code: String?) {
        code?.let { qrCode ->
            _qrCodeAnalyseState.value = State.Loading
            val elements = qrCode.split("#ysnp#")
            val ipRegex = "(\\d+\\.){3}\\d+(:\\d{4})?".toRegex()
            if (elements.size == 2 && elements[0].matches(ipRegex) && elements[1].contains("KEY")) {
                viewModelScope.launch {
                    kotlin.runCatching {
                        desktopRepository.updateDesktopInfo(
                            "http://" + elements[0],
                            cleanUpKey(elements[1])
                        )
                    }
                        .onSuccess { _qrCodeAnalyseState.value = State.Success(Unit) }
                        .onFailure { _qrCodeAnalyseState.value = State.Error }
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
        while (lines.last().isEmpty()) {
            lines.removeAt(lines.size - 1)
        }
        lines.removeAt(lines.size - 1)
        return lines.joinToString("\n").replace("\n", "")
    }

}