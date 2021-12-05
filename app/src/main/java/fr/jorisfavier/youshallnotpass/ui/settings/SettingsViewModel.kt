package fr.jorisfavier.youshallnotpass.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.repository.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.utils.CoroutineDispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferenceDataSource,
    private val itemRepository: ItemRepository,
    private val externalItemRepository: ExternalItemRepository,
    private val cryptoManager: CryptoManager,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    val themeValues: Array<String>

    val themeEntries: IntArray

    init {
        val values = mutableListOf(
            AppCompatDelegate.MODE_NIGHT_NO.toString(),
            AppCompatDelegate.MODE_NIGHT_YES.toString()
        )

        val entries = mutableListOf(
            R.string.light,
            R.string.dark
        )
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            values.add(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY.toString())
            entries.add(R.string.battery_saver)
        } else {
            values.add(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())
            entries.add(R.string.system_default)
        }
        themeValues = values.toTypedArray()
        themeEntries = entries.toIntArray()
    }

    fun getDefaultThemeValue(currentNightMode: Int): Flow<String?> = flow {
        val currentTheme = if (!appPreferences.getTheme().isNullOrEmpty()) {
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO.toString()
                else -> AppCompatDelegate.MODE_NIGHT_YES.toString()
            }
        } else {
            Timber.d("No preference found for theme")
            null
        }
        emit(currentTheme)
    }

    fun exportPasswords(password: String?): Flow<Result<Intent>> {
        return flow {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "plain/text"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val items = itemRepository.getAllItems().first().map {
                val itemPassword =
                    cryptoManager.decryptData(it.password, it.initializationVector)
                ExternalItem(it.title, it.login, itemPassword)
            }
            val uri = externalItemRepository.saveExternalItems(items, password)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            emit(Result.success(intent))
        }.flowOn(dispatchers.io).catch { e ->
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
