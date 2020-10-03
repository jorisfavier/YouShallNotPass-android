package fr.jorisfavier.youshallnotpass.ui.settings

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.R
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
        private val sharedPreferences: SharedPreferences
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

    private fun createCsvExport(): ByteArray {

    }
}