package fr.jorisfavier.youshallnotpass.ui.search

import android.content.ClipData
import android.content.ClipboardManager
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.ui.settings.SettingsFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
            if (query.isNotBlank() && query.isNotEmpty()) {
                emit(itemRepository.searchItem("$query%"))
            } else if (sharedPreferences.getBoolean(
                    SettingsFragment.ALL_ITEMS_PREFERENCE_KEY,
                    false
                )
            ) {
                emit(itemRepository.getAllItems())
            } else {
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
            if (key == SettingsFragment.ALL_ITEMS_PREFERENCE_KEY) {
                search.value = search.value
            }
        }

    private val isSearchEmpty: Boolean
        get() = search.value.orEmpty().isEmpty() || search.value.orEmpty().isBlank()

    init {
        noResultTextIdRes.value = R.string.common_no_results_found
        noResultTextIdRes.addSource(search) {
            noResultTextIdRes.value = if (isSearchEmpty) R.string.use_the_search else R.string.common_no_results_found
        }
    }

    fun deleteItem(item: Item): Flow<Result<Unit>> {
        return flow {
            try {
                itemRepository.deleteItem(item)
                emit(Result.success(Unit))
            } catch (e: Exception) {
                emit(Result.failure<Unit>(e))
            }
        }
    }

    fun decryptPassword(item: Item): String {
        return cryptoManager.decryptData(item.password, item.initializationVector)
    }

    fun copyToClipboard(item: Item, type: ItemDataType) {
        val data = when (type) {
            ItemDataType.PASSWORD -> decryptPassword(item)
            ItemDataType.LOGIN -> item.login
        }
        val clip = ClipData.newPlainText(type.name, data)
        clipboardManager.setPrimaryClip(clip)
    }

    fun refreshItems() {
        //If the user didn't search for something it will force an item refresh
        if (isSearchEmpty) {
            search.value = ""
        }
    }
}
