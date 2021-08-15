package fr.jorisfavier.youshallnotpass.ui.item

import androidx.lifecycle.*
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.utils.PasswordOptions
import fr.jorisfavier.youshallnotpass.utils.PasswordUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ItemEditViewModel @Inject constructor(
    private val cryptoManager: ICryptoManager,
    private val itemRepository: IItemRepository
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

    fun initData(itemId: Int, itemName: String?) {
        if (itemId > 0) {
            viewModelScope.launch {
                currentItem = runCatching { itemRepository.getItemById(itemId) }.getOrNull()
                currentItem?.let {
                    _createOrUpdateText.value = R.string.item_update
                    name.value = it.title
                    password.value = cryptoManager.decryptData(it.password, it.initializationVector)
                    login.value = it.login.orEmpty()
                }
            }
        } else if (itemName != null) {
            name.value = itemName.orEmpty()
        }
    }

    fun generateSecurePassword() {
        passwordLengthValue.value?.let {
            password.value = PasswordUtil.getSecurePassword(passwordOptions, it)
        }
    }

    fun updateOrCreateItem(): Flow<Result<Int>> {
        return flow {
            val passwordValue = password.value
            val nameValue = name.value?.capitalize(Locale.ROOT)
            val id = currentItem?.id ?: 0
            if (passwordValue != null && nameValue != null) {
                val encryptedData = cryptoManager.encryptData(passwordValue)

                if (id == 0 && itemRepository.searchItem(nameValue).isNotEmpty()) {
                    emit(Result.failure<Int>(YsnpException(R.string.item_already_exist)))
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
                    val successResourceId =
                        if (id == 0) R.string.item_creation_success else R.string.item_update_success
                    emit(Result.success(successResourceId))
                }
            } else {
                emit(Result.failure<Int>(YsnpException(R.string.item_name_or_password_missing)))
            }
        }.catch {
            emit(Result.failure(YsnpException(R.string.error_occurred)))
        }
    }


}
