package fr.jorisfavier.youshallnotpass.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.repository.IExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val itemRepository: IItemRepository,
    private val externalItemRepository: IExternalItemRepository,
    private val cryptoManager: ICryptoManager,
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

    fun getDefaultThemeValue(currentNightMode: Int): String? {
        return if (!sharedPreferences.contains(SettingsFragment.THEME_PREFERENCE_KEY)) {
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO.toString()
                else -> AppCompatDelegate.MODE_NIGHT_YES.toString()
            }
        } else {
            null
        }
    }

    fun exportPasswords(password: String?): Flow<Result<Intent>> {
        return flow {
            try {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "plain/text"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val items = itemRepository.getAllItems().map {
                    val itemPassword = cryptoManager.decryptData(it.password, it.initializationVector)
                    ExternalItem(it.title, itemPassword)
                }
                val uri = externalItemRepository.saveExternalItems(items, password)
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                emit(Result.success(intent))
            } catch (e: Exception) {
                emit(Result.failure<Intent>(e))
            }
        }
    }
}