package fr.jorisfavier.youshallnotpass.ui.item

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception
import javax.inject.Inject

class ItemEditViewModel @Inject constructor(
        private val cryptoManager: ICryptoManager,
        private val itemRepository: IItemRepository
) : ViewModel() {

    val name = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    fun generateSecurePassword(){
        //TODO
    }

    fun addNewItem(): Flow<Result<Unit>> {
        return flow {
            val passwordValue = password.value
            val nameValue = name.value
            if (passwordValue != null && nameValue != null){
                val cipher = cryptoManager.getInitializedCipherForEncryption()
                val encryptedData = cryptoManager.encryptData(passwordValue, cipher)
                itemRepository.storeItem(nameValue, encryptedData.ciphertext, encryptedData.initializationVector)
                emit(Result.success(Unit))
            } else {
                emit(Result.failure<Unit>(Exception()))
            }
        }

    }
}
