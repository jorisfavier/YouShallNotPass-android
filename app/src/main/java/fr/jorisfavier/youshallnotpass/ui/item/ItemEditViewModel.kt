package fr.jorisfavier.youshallnotpass.ui.item

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.utils.PasswordOptions
import fr.jorisfavier.youshallnotpass.utils.PasswordUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    private val passwordOptions: Int
        get() {
            var result = 0
            result += if (hasUppercase.value!!) PasswordOptions.UPPERCASE.value else 0
            result += if (hasSymbol.value!!) PasswordOptions.SYMBOL.value else 0
            result += if (hasNumber.value!!) PasswordOptions.NUMBER.value else 0
            return result
        }

    fun generateSecurePassword() {
        passwordLength.value?.let {
            password.value = PasswordUtil.getSecurePassword(passwordOptions, it)
        }
    }

    fun addNewItem(): Flow<Result<Unit>> {
        return flow {
            val passwordValue = password.value
            val nameValue = name.value
            if (passwordValue != null && nameValue != null) {
                val cipher = cryptoManager.getInitializedCipherForEncryption()
                val encryptedData = cryptoManager.encryptData(passwordValue, cipher)
                itemRepository.storeItem(nameValue, encryptedData.ciphertext, encryptedData.initializationVector)
                emit(Result.success(Unit))
            } else {
                emit(Result.failure<Unit>(Exception()))
            }
        }
    }

    fun setPasswordLength(newValue: Int) {
        passwordLength.value = PasswordUtil.defaultSize + newValue
    }


}
