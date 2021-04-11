package fr.jorisfavier.youshallnotpass.ui.settings

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.autofill.AutofillManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.BuildConfig
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource.Companion.HIDE_ITEMS_PREFERENCE_KEY
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource.Companion.THEME_PREFERENCE_KEY
import fr.jorisfavier.youshallnotpass.ui.common.BlinkPreference
import fr.jorisfavier.youshallnotpass.ui.home.HomeViewModel
import fr.jorisfavier.youshallnotpass.utils.extensions.getEntryforValue
import fr.jorisfavier.youshallnotpass.utils.extensions.toast
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val args: SettingsFragmentArgs by navArgs()

    val viewModel: SettingsViewModel by viewModels { viewModelFactory }

    private val homeViewModel: HomeViewModel by activityViewModels { viewModelFactory }

    private lateinit var allItemsVisibilityPreference: SwitchPreferenceCompat
    private lateinit var importPreference: Preference
    private lateinit var exportPreference: Preference
    private lateinit var appThemePreference: ListPreference
    private lateinit var versionPreference: Preference
    private lateinit var deleteAllPreference: Preference
    private lateinit var desktopPreference: Preference
    private lateinit var autofillPreference: SwitchPreferenceCompat

    private val requestAutofill =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                autofillPreference.isChecked = true
            }
        }

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
        allItemsVisibilityPreference = findPreference(HIDE_ITEMS_PREFERENCE_KEY)!!
        importPreference = findPreference(KEY_IMPORT)!!
        exportPreference = findPreference(KEY_EXPORT)!!
        appThemePreference = findPreference(THEME_PREFERENCE_KEY)!!
        versionPreference = findPreference(KEY_APP_VERSION)!!
        deleteAllPreference = findPreference(KEY_DELETE_ALL)!!
        desktopPreference = findPreference(KEY_DESKTOP)!!
        autofillPreference = findPreference(KEY_AUTOFILL)!!

        initAppThemePreference()
        initExportPreference()
        initImportPreference()
        initAboutPreference()
        initDeleteAllPreference()
        initDesktopPreference()
        playFocusAnimationIfNeeded()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initAutofillPreference()
        }
    }

    private fun initAppThemePreference() {
        appThemePreference.entryValues = viewModel.themeValues
        appThemePreference.entries = viewModel.themeEntries.map { getString(it) }.toTypedArray()
        appThemePreference.summary = appThemePreference.getEntryforValue(appThemePreference.value)
        appThemePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                (newValue as? String)?.toIntOrNull()?.let {
                    AppCompatDelegate.setDefaultNightMode(it)
                    appThemePreference.summary = appThemePreference.getEntryforValue(newValue)
                } ?: run {
                    context?.toast(R.string.error_changing_theme)
                }
                true
            }
        lifecycleScope.launchWhenCreated {
            viewModel.getDefaultThemeValue(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
                .collect {
                    appThemePreference.value = it
                }
        }
    }

    private fun initExportPreference() {
        exportPreference.setOnPreferenceClickListener {
            val dialog = ExportDialogFragment(::exportPasswords)
            dialog.show(
                requireActivity().supportFragmentManager,
                ExportDialogFragment::class.java.simpleName
            )
            true
        }
    }

    private fun initImportPreference() {
        importPreference.setOnPreferenceClickListener {
            homeViewModel.ignoreNextPause()
            val direction =
                SettingsFragmentDirections.actionSettingsFragmentToImportPasswordActivity()
            findNavController().navigate(direction)
            true
        }
    }

    private fun initAboutPreference() {
        versionPreference.summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }

    private fun initDeleteAllPreference() {
        deleteAllPreference.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_all)
                .setMessage(R.string.delete_all_confirmation)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    lifecycleScope.launchWhenCreated {
                        viewModel.deleteAllItems().collect {
                            val messageResId =
                                if (it.isSuccess) R.string.delete_all_successful else R.string.error_occurred
                            requireContext().toast(messageResId)
                        }
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
            true
        }
    }

    private fun initDesktopPreference() {
        desktopPreference.setViewId(View.generateViewId())
        desktopPreference.setOnPreferenceClickListener {
            homeViewModel.ignoreNextPause()
            val direction =
                SettingsFragmentDirections.actionSettingsFragmentToDesktopConnectionActivity()
            findNavController().navigate(direction)
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAutofillPreference() {
        val autofillManager = requireContext().getSystemService(AutofillManager::class.java)
        autofillPreference.isVisible = true
        autofillPreference.isChecked = autofillManager.hasEnabledAutofillServices()
        autofillPreference.setOnPreferenceChangeListener { _, checked ->
            if (checked == true) {
                homeViewModel.ignoreNextPause()
                val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                    data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                }
                requestAutofill.launch(intent)
                false
            } else {
                autofillManager.disableAutofillServices()
                true
            }
        }
    }

    private fun playFocusAnimationIfNeeded() {
        args.highlightItem?.let {
            findPreference<BlinkPreference>(it)?.blink()
        }
    }

    private fun exportPasswords(password: String?) {
        lifecycleScope.launchWhenStarted {
            viewModel.exportPasswords(password).collect {
                if (it.isFailure) {
                    context?.toast(R.string.password_export_failed)
                } else {
                    homeViewModel.ignoreNextPause()
                    requireActivity().startActivity(it.getOrThrow())
                }
            }
        }
    }

    companion object {
        const val KEY_IMPORT = "import"
        const val KEY_EXPORT = "export"
        const val KEY_APP_VERSION = "appVersion"
        const val KEY_DELETE_ALL = "deleteAll"
        const val KEY_DESKTOP = "desktop"
        const val KEY_AUTOFILL = "autofill"
    }
}