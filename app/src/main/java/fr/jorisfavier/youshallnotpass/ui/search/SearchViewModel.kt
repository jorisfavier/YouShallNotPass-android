package fr.jorisfavier.youshallnotpass.ui.search

import android.content.ClipData
import android.content.ClipboardManager
import android.content.SharedPreferences
import androidx.lifecycle.*
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.ui.settings.SettingsFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val itemRepository: IItemRepository,
    private val cryptoManager: ICryptoManager,
    private val clipboardManager: ClipboardManager,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    val search = MutableLiveData("")

    val results: LiveData<List<Item>> = search.switchMap { query ->
        liveData {
            try {
                when {
                    query.isNotBlank() && query.isNotEmpty() -> {
                        emit(itemRepository.searchItem("$query%"))
                    }
                    !hideAllItems -> {
                        emit(itemRepository.getAllItems())
                    }
                    else -> {
                        emit(listOf<Item>())
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error while searching for items")
                emit(listOf<Item>())
            }
        }
    }

    val hasNoResult: LiveData<Boolean> = results.map { listItem ->
        listItem.count() == 0
    }

    val noResultTextIdRes = MediatorLiveData<Int>()

    val onSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == SettingsFragment.HIDE_ITEMS_PREFERENCE_KEY) {
                search.value = search.value
            }
        }

    private val isSearchEmpty: Boolean
        get() = search.value.orEmpty().isEmpty() || search.value.orEmpty().isBlank()
    private val hideAllItems: Boolean
        get() = sharedPreferences.getBoolean(SettingsFragment.HIDE_ITEMS_PREFERENCE_KEY, false)

    init {
        noResultTextIdRes.value = R.string.no_results_found
        noResultTextIdRes.addSource(search) {
            noResultTextIdRes.value = when {
                isSearchEmpty && !hideAllItems -> R.string.no_item_yet
                isSearchEmpty && hideAllItems -> R.string.use_the_search
                else -> R.string.no_results_found
            }
        }
    }

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
}
