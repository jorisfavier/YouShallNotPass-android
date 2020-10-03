package fr.jorisfavier.youshallnotpass.ui.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.utils.getEntryforValue
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val THEME_PREFERENCE_KEY = "theme"
        const val ALL_ITEMS_PREFERENCE_KEY = "allItems"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val viewModel: SettingsViewModel by viewModels { viewModelFactory }

    private lateinit var allItemsVisibilityPreference: SwitchPreferenceCompat
    private lateinit var importPreference: Preference
    private lateinit var exportPreference: Preference
    private lateinit var appThemePreference: ListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()
        allItemsVisibilityPreference = findPreference("allItems")!!
        importPreference = findPreference("import")!!
        exportPreference = findPreference("export")!!
        appThemePreference = findPreference("theme")!!

        initAppThemePreference()
        initExportPreference()
    }

    private fun initAppThemePreference() {
        appThemePreference.entryValues = viewModel.themeValues
        appThemePreference.entries = viewModel.themeEntries.map { getString(it) }.toTypedArray()
        viewModel.getDefaultThemeValue(
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        )?.let {
            appThemePreference.value = it
        }
        appThemePreference.summary = appThemePreference.getEntryforValue(appThemePreference.value)
        appThemePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                appThemePreference.summary = appThemePreference.getEntryforValue(newValue)
                true
            }
    }

    private fun initExportPreference() {
        val customView = layoutInflater.inflate(R.layout.dialog_settings_export, null)
        exportPreference.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.export_password)
                .setView(customView)
                .setPositiveButton(R.string.export_password) { dialogInterface, _ ->
                    Toast.makeText(context, "test", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            true
        }
    }
}