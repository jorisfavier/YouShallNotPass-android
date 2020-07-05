package fr.jorisfavier.youshallnotpass.ui.search

import android.content.ClipData
import android.content.ClipboardManager
import androidx.lifecycle.*
import fr.jorisfavier.youshallnotpass.data.model.Item
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor(
        private val itemRepository: IItemRepository,
        private val cryptoManager: ICryptoManager,
        private val clipboardManager: ClipboardManager
) : ViewModel() {

    val search = MutableLiveData<String>()

    val results: LiveData<List<Item>> = Transformations.switchMap(search) { query ->
        liveData {
            if (query.isNotBlank() && query.isNotEmpty()) {
                emit(itemRepository.searchItem("$query%"))
            } else {
                emit(listOf())
            }
        }
    }
    val hasNoResult: LiveData<Boolean> = Transformations.map(results) { listItem ->
        listItem.count() == 0
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemRepository.deleteItem(item)
        }
    }

    fun decryptPassword(item: Item): String {
        val cipher =
                cryptoManager.getInitializedCipherForDecryption(item.initializationVector)
        return cryptoManager.decryptData(item.password, cipher)
    }

    fun copyPasswordToClipboard(item: Item) {
        val clip = ClipData.newPlainText("password", decryptPassword(item))
        clipboardManager.primaryClip = clip
    }
}
