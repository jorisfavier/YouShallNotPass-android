package fr.jorisfavier.youshallnotpass.ui.search

import android.content.ClipData
import android.content.ClipboardManager
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.ui.settings.SettingsFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val itemRepository: IItemRepository,
    private val cryptoManager: ICryptoManager,
    private val clipboardManager: ClipboardManager,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    val search = MutableLiveData<String>("")

    val results: LiveData<List<Item>> = Transformations.switchMap(search) { query ->
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
    val hasNoResult: LiveData<Boolean> = Transformations.map(results) { listItem ->
        listItem.count() == 0
    }

    val onSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == SettingsFragment.ALL_ITEMS_PREFERENCE_KEY) {
                search.value = search.value
            }
        }

    @ExperimentalCoroutinesApi
    fun deleteItem(item: Item): Flow<Result<Unit>> {
        return flow {
            itemRepository.deleteItem(item)
            search.value = search.value
            emit(Result.success(Unit))
        }.catch {
            emit(Result.failure(Exception()))
        }
    }

    fun decryptPassword(item: Item): String {
        return cryptoManager.decryptData(item.password, item.initializationVector)
    }

    fun copyPasswordToClipboard(item: Item) {
        val clip = ClipData.newPlainText("password", decryptPassword(item))
        clipboardManager.setPrimaryClip(clip)
    }
}
