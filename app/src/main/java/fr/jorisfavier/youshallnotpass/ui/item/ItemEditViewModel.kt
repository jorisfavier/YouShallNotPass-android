package fr.jorisfavier.youshallnotpass.ui.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.exception.ItemAlreadyExistException
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.utils.PasswordOptions
import fr.jorisfavier.youshallnotpass.utils.PasswordUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ItemEditViewModel @Inject constructor(
    private val cryptoManager: ICryptoManager,
    private val itemRepository: IItemRepository
) : ViewModel() {

    val name = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val passwordLength = MutableLiveData(PasswordUtil.defaultSize)
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
            return result
        }

    fun initData(itemId: Int) {
        if (itemId > 0) {
            viewModelScope.launch {
                currentItem = itemRepository.getItemById(itemId)
                currentItem?.let {
                    _createOrUpdateText.value = R.string.item_update
                    name.value = it.title
                    password.value = cryptoManager.decryptData(it.password, it.initializationVector)
                }
            }
        }
    }

    fun generateSecurePassword() {
        passwordLength.value?.let {
            password.value = PasswordUtil.getSecurePassword(passwordOptions, it)
        }
    }

    fun updateOrCreateItem(): Flow<Result<Int>> {
        return flow {
            val passwordValue = password.value
            val nameValue = name.value
            val id = currentItem?.id ?: 0
            if (passwordValue != null && nameValue != null) {
                val encryptedData = cryptoManager.encryptData(passwordValue)

                if (id == 0 && itemRepository.searchItem(nameValue).isNotEmpty()) {
                    emit(Result.failure(ItemAlreadyExistException()))
                } else {
                    itemRepository.updateOrCreateItem(
                        Item(id, nameValue, encryptedData.ciphertext, encryptedData.initializationVector)
                    )
                    val successResourceId =
                        if (id == 0) R.string.item_creation_success else R.string.item_update_success
                    emit(Result.success(successResourceId))
                }
            } else {
                emit(Result.failure(Exception()))
            }
        }
    }

    fun setPasswordLength(newValue: Int) {
        passwordLength.value = PasswordUtil.defaultSize + newValue
    }


}
