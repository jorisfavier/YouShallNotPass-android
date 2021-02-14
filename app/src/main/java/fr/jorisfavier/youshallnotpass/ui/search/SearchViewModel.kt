package fr.jorisfavier.youshallnotpass.ui.search

import android.content.ClipData
import android.content.ClipboardManager
import androidx.lifecycle.*
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.repository.DesktopRepository
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.utils.default
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val itemRepository: IItemRepository,
    private val cryptoManager: ICryptoManager,
    private val clipboardManager: ClipboardManager,
    private val appPreference: AppPreferenceDataSource,
    private val desktopRepository: DesktopRepository
) : ViewModel() {

    val search = MutableLiveData("")

    val results = search.switchMap { query ->
        liveData<List<Item>> {
            try {
                val hideAll = appPreference.getShouldHideItems()
                when {
                    query.isNotBlank() && query.isNotEmpty() -> {
                        emit(itemRepository.searchItem("%$query%"))
                    }
                    !hideAll -> {
                        emit(itemRepository.getAllItems())
                    }
                    else -> {
                        emit(listOf())
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error while searching for items")
                emit(listOf())
            }
        }
    }

    val hasNoResult: LiveData<Boolean> = results.map { listItem ->
        listItem.count() == 0
    }

    val noResultTextIdRes = search.switchMap { search ->
        liveData {
            val isSearchEmpty = search.isEmpty() || search.isBlank()
            val hideAll = appPreference.getShouldHideItems()
            val res = when {
                isSearchEmpty && !hideAll -> R.string.no_item_yet
                isSearchEmpty && hideAll -> R.string.use_the_search
                else -> R.string.no_results_found
            }
            emit(res)
        }
    }.default(R.string.no_results_found)


    fun deleteItem(item: Item): Flow<Result<Unit>> {
        return flow {
            itemRepository.deleteItem(item)
            emit(Result.success(Unit))
        }.catch { e ->
            Timber.e(e, "Error during item deletion")
            emit(Result.failure(e))
        }
    }

    fun decryptPassword(item: Item): Result<String> = runCatching {
        cryptoManager.decryptData(item.password, item.initializationVector)
    }

    fun copyToClipboard(item: Item, type: ItemDataType): Result<Int> = runCatching {
        val (data, resId) = when (type) {
            ItemDataType.PASSWORD -> (decryptPassword(item).getOrThrow() to R.string.copy_password_to_clipboard_success)
            ItemDataType.LOGIN -> (item.login to R.string.copy_login_to_clipboard_success)
        }
        val clip = ClipData.newPlainText(type.name, data)
        clipboardManager.setPrimaryClip(clip)
        return Result.success(resId)
    }

    fun refreshItems() {
        search.value = search.value
    }

    fun sendToDesktop(item: Item, type: ItemDataType) {
        viewModelScope.launch {
            kotlin.runCatching {
                val data = when (type) {
                    ItemDataType.PASSWORD -> decryptPassword(item).getOrThrow()
                    ItemDataType.LOGIN -> item.login.orEmpty()
                }
                desktopRepository.sendData(data)
            }
        }
    }

}
