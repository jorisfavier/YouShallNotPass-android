package fr.jorisfavier.youshallnotpass.ui.settings

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.repository.AppPreferenceRepository
import fr.jorisfavier.youshallnotpass.repository.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.utils.CoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferenceRepository: AppPreferenceRepository,
    private val itemRepository: ItemRepository,
    private val externalItemRepository: ExternalItemRepository,
    private val cryptoManager: CryptoManager,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    val hideAllItems = appPreferenceRepository.shouldHideItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    val selectedTheme = appPreferenceRepository.theme
        .map { themeValue ->
            themeEntries[themeValue ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM]
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    val themeEntries = buildMap<Int, Int> {
        put(AppCompatDelegate.MODE_NIGHT_NO, R.string.light)
        put(AppCompatDelegate.MODE_NIGHT_YES, R.string.dark)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            put(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, R.string.battery_saver)
        } else {
            put(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.system_default)
        }
    }

    fun setHideAllItems(shouldHideAll: Boolean) {
        viewModelScope.launch {
            appPreferenceRepository.setShouldHideItems(shouldHideAll)
        }
    }

    fun selectTheme(themeValue: Int) {
        viewModelScope.launch {
            appPreferenceRepository.setTheme(themeValue)
        }
    }

    fun exportPasswords(password: String?): Flow<Result<Intent>> {
        return flow {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "plain/text"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val items = itemRepository.getAllItems().first().map {
                val itemPassword =
                    cryptoManager.decryptData(it.password, it.initializationVector).getOrThrow()
                ExternalItem(it.title, it.login, itemPassword)
            }
            val uri = externalItemRepository.saveExternalItems(items, password).getOrThrow()
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            emit(Result.success(intent))
        }
            .flowOn(dispatchers.io)
            .catch { e ->
                Timber.e(e, "Error while exporting items")
                emit(Result.failure(e))
            }
    }

    fun deleteAllItems(): Flow<Result<Unit>> {
        return flow {
            itemRepository.deleteAllItems()
            emit(Result.success(Unit))
        }.catch {
            Timber.e(it, "An error occurred while deleting all the items")
            emit(Result.failure(it))
        }
    }
}
