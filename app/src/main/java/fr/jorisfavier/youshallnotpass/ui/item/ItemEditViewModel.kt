package fr.jorisfavier.youshallnotpass.ui.item

import android.content.ClipData
import android.content.ClipboardManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
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
import javax.inject.Inject

@HiltViewModel
class ItemEditViewModel @Inject constructor(
    private val cryptoManager: CryptoManager,
    private val itemRepository: ItemRepository,
    private val clipboardManager: ClipboardManager,
) : ViewModel() {

    private val _currentItem = MutableLiveData<Item?>(null)
    val currentItem: LiveData<Item?> = _currentItem

    private val _passwordLength = MutableLiveData(PasswordUtil.MINIMUM_SECURE_SIZE)
    val passwordLength: LiveData<Int> = _passwordLength

    val password: LiveData<String?> = _currentItem
        .switchMap { item ->
            liveData {
                emit(
                    item?.let {
                        cryptoManager.decryptData(item.password, item.initializationVector)
                            .getOrDefault("")
                    }
                )
            }
        }

    private val _createOrUpdateText = MutableLiveData(R.string.item_create)
    val createOrUpdateText: LiveData<Int> = _createOrUpdateText

    fun initData(itemId: Int, itemName: String? = null) {
        if (itemId > 0) {
            viewModelScope.launch {
                val item = itemRepository.getItemById(itemId).getOrNull() ?: return@launch
                _currentItem.value = item
                _createOrUpdateText.value = R.string.item_update
            }
        } else if (itemName != null) {
            _currentItem.value = Item(
                id = 0,
                title = itemName,
                password = ByteArray(0),
                initializationVector = ByteArray(0),
            )
        }
    }

    fun onPasswordLengthChanged(length: Int) {
        _passwordLength.value = length + PasswordUtil.MINIMUM_SECURE_SIZE
    }

    fun generateSecurePassword(
        hasUppercase: Boolean,
        hasSymbol: Boolean,
        hasNumber: Boolean,
    ): String {
        val pwdLength = _passwordLength.value!!
        var passwordOptions = 0
        passwordOptions += if (hasUppercase) PasswordOptions.UPPERCASE.value else 0
        passwordOptions += if (hasSymbol) PasswordOptions.SYMBOL.value else 0
        passwordOptions += if (hasNumber) PasswordOptions.NUMBER.value else 0
        return PasswordUtil.getSecurePassword(passwordOptions, pwdLength)
    }

    fun updateOrCreateItem(
        name: String?,
        password: String?,
        login: String?,
    ): Flow<Result<Int>> {
        return flow {
            val nameValue = name?.titleCase()
            val id = _currentItem.value?.id ?: 0
            if (password != null && nameValue != null) {
                val encryptedData = cryptoManager.encryptData(password).getOrElse {
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
                            login = login,
                            password = encryptedData.ciphertext,
                            initializationVector = encryptedData.initializationVector,
                            packageCertificate = _currentItem.value?.packageCertificate.orEmpty()
                        )
                    )
                        .onSuccess {
                            val successResourceId = if (id == 0) {
                                val clip =
                                    ClipData.newPlainText(ItemDataType.PASSWORD.name, password)
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
