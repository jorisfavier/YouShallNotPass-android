package fr.jorisfavier.youshallnotpass.ui.item

import android.content.ClipData
import android.content.ClipboardManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.utils.PasswordOptions
import fr.jorisfavier.youshallnotpass.utils.PasswordUtil
import fr.jorisfavier.youshallnotpass.utils.extensions.titleCase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ItemEditViewModel @Inject constructor(
    private val cryptoManager: CryptoManager,
    private val itemRepository: ItemRepository,
    private val clipboardManager: ClipboardManager,
) : ViewModel() {

    val name = MutableLiveData<String>()
    val login = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val passwordLength = MutableLiveData(0)
    val passwordLengthValue = passwordLength.map { it + PasswordUtil.MINIMUM_SECURE_SIZE }
    val hasUppercase = MutableLiveData(true)
    val hasSymbol = MutableLiveData(true)
    val hasNumber = MutableLiveData(true)
    private var currentItem: Item? = null

    private val _createOrUpdateText = MutableLiveData(R.string.item_create)
    val createOrUpdateText: LiveData<Int> = _createOrUpdateText

    private val passwordOptions: Int
        get() {
            var result = 0
            result += if (hasUppercase.value!!) PasswordOptions.UPPERCASE.value else 0
            result += if (hasSymbol.value!!) PasswordOptions.SYMBOL.value else 0
            result += if (hasNumber.value!!) PasswordOptions.NUMBER.value else 0
            Timber.d("current password options: Uppercase=${hasUppercase.value} - Symbol=${hasSymbol.value} - Number=${hasNumber.value}")
            return result
        }

    fun initData(itemId: Int, itemName: String? = null) {
        if (itemId > 0) {
            viewModelScope.launch {
                val item = itemRepository.getItemById(itemId).getOrNull() ?: return@launch
                currentItem = item
                _createOrUpdateText.value = R.string.item_update
                name.value = item.title
                password.value = cryptoManager.decryptData(item.password, item.initializationVector)
                    .getOrDefault("")
                login.value = item.login.orEmpty()
            }
        } else if (itemName != null) {
            name.value = itemName
        }
    }

    fun generateSecurePassword() {
        val pwdLength = passwordLengthValue.value ?: return
        password.value = PasswordUtil.getSecurePassword(passwordOptions, pwdLength)
    }

    fun updateOrCreateItem(): Flow<Result<Int>> {
        return flow {
            val passwordValue = password.value
            val nameValue = name.value?.titleCase()
            val id = currentItem?.id ?: 0
            if (passwordValue != null && nameValue != null) {
                val encryptedData = cryptoManager.encryptData(passwordValue).getOrElse {
                    emit(Result.failure(YsnpException(R.string.error_occurred)))
                    currentCoroutineContext().cancel()
                    return@flow
                }
                if (id == 0 && itemRepository.searchItem(nameValue).getOrDefault(emptyList())
                        .isNotEmpty()
                ) {
                    emit(Result.failure(YsnpException(R.string.item_already_exist)))
                } else {
                    itemRepository.updateOrCreateItem(
                        Item(
                            id = id,
                            title = nameValue,
                            login = login.value,
                            password = encryptedData.ciphertext,
                            initializationVector = encryptedData.initializationVector,
                            packageCertificate = currentItem?.packageCertificate.orEmpty()
                        )
                    )
                        .onSuccess {
                            val successResourceId = if (id == 0) {
                                val clip =
                                    ClipData.newPlainText(ItemDataType.PASSWORD.name, passwordValue)
                                clipboardManager.setPrimaryClip(clip)
                                R.string.item_creation_success
                            } else {
                                R.string.item_update_success
                            }
                            emit(Result.success(successResourceId))
                        }
                        .onFailure {
                            emit(Result.failure(YsnpException(R.string.error_occurred)))
                        }
                }
            } else {
                emit(Result.failure(YsnpException(R.string.item_name_or_password_missing)))
            }
        }
    }


}
