package fr.jorisfavier.youshallnotpass.ui.settings

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import fr.jorisfavier.youshallnotpass.BuildConfig
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentSettingsBinding
import fr.jorisfavier.youshallnotpass.ui.home.HomeViewModel
import fr.jorisfavier.youshallnotpass.utils.autoCleared
import fr.jorisfavier.youshallnotpass.utils.extensions.getThemeColor
import fr.jorisfavier.youshallnotpass.utils.extensions.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val args: SettingsFragmentArgs by navArgs()

    private var binding: FragmentSettingsBinding by autoCleared()

    val viewModel: SettingsViewModel by viewModels()

    private val homeViewModel: HomeViewModel by activityViewModels()

    private val requestAutofill =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.autofill.isChecked = true
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.topAppBar.apply {
            applyInsetter {
                type(statusBars = true) { padding() }
            }
            setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        binding.container.applyInsetter {
            type(navigationBars = true) { padding() }
        }

        initObservers()
        initHideAllPreference()
        initAppThemePreference()
        initExportPreference()
        initImportPreference()
        initAboutPreference()
        initDeleteAllPreference()
        initDesktopPreference()
        playFocusAnimationIfNeeded()
        initPrivacyPolicyPreference()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initAutofillPreference()
        }
    }

    private fun initHideAllPreference() {
        binding.hideAll.text = buildTitleAndExplanation(
            title = getString(R.string.display_all_items),
            explanation = getString(R.string.all_items_settings_description)
        )
        binding.hideAll.setOnCheckedChangeListener { _, shouldHideAll ->
            viewModel.setHideAllItems(shouldHideAll)
        }
    }

    private fun initAppThemePreference() {
        val entries = viewModel.themeEntries
        val popup = PopupMenu(requireContext(), binding.theme).apply {
            menu.apply {
                entries.keys.forEach { themeValue ->
                    add(
                        Menu.NONE,
                        themeValue,
                        Menu.NONE,
                        entries[themeValue]?.let { getString(it) } ?: ""
                    )
                }
            }
        }
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            viewModel.selectTheme(menuItem.itemId)
            return@setOnMenuItemClickListener true
        }
        binding.theme.setOnClickListener {
            popup.show()
        }
    }

    private fun initExportPreference() {
        binding.exportItems.setOnClickListener {
            ExportDialogFragment().show(
                childFragmentManager,
                ExportDialogFragment.TAG,
            )
        }
    }

    private fun initImportPreference() {
        binding.importItems.setOnClickListener {
            homeViewModel.ignoreNextPause()
            val direction =
                SettingsFragmentDirections.actionSettingsFragmentToImportPasswordActivity()
            findNavController().navigate(direction)
        }
    }

    private fun initAboutPreference() {
        binding.version.text = buildTitleAndExplanation(
            title = getString(R.string.version),
            explanation = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        )
    }

    private fun initDeleteAllPreference() {
        binding.deleteAll.setOnClickListener {
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
        }
    }

    private fun initDesktopPreference() {
        binding.desktop.text = buildTitleAndExplanation(
            title = getString(R.string.ysnp_desktop),
            explanation = getString(R.string.ysnp_desktop_summary)
        )
        binding.desktop.setOnClickListener {
            homeViewModel.ignoreNextPause()
            val direction =
                SettingsFragmentDirections.actionSettingsFragmentToDesktopConnectionActivity()
            findNavController().navigate(direction)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAutofillPreference() {
        val autofillManager = requireContext().getSystemService(AutofillManager::class.java)
        with(binding.autofill) {
            text = buildTitleAndExplanation(
                title = getString(R.string.autofill),
                explanation = getString(R.string.autofill_explanation),
            )
            isVisible = true
            isChecked = autofillManager.hasEnabledAutofillServices()
            setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    homeViewModel.ignoreNextPause()
                    val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                        data = "package:${BuildConfig.APPLICATION_ID}".toUri()
                    }
                    requestAutofill.launch(intent)
                } else {
                    autofillManager.disableAutofillServices()
                }
            }
        }
    }

    private fun initPrivacyPolicyPreference() {
        binding.privacyPolicy.setOnClickListener {
            val direction =
                SettingsFragmentDirections.actionSettingsFragmentToPrivacyPolicyFragment()
            findNavController().navigate(direction)
        }
    }

    private fun playFocusAnimationIfNeeded() {
        val highlightItem = args.highlightItem.takeIf { it > 0 } ?: return
        view?.findViewById<View>(highlightItem)?.apply {
            setBackgroundResource(R.drawable.blink)
            (background as? AnimationDrawable)?.start()
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    viewModel.selectedTheme.collectLatest { themeResId ->
                        val title = getString(R.string.app_theme)
                        binding.theme.text = if (themeResId != null) {
                            buildTitleAndExplanation(
                                title = title,
                                explanation = getString(themeResId),
                            )
                        } else title
                    }
                }
                launch {
                    viewModel.hideAllItems.collectLatest { shouldHideAllItems ->
                        binding.hideAll.isChecked = shouldHideAllItems
                    }
                }
            }
        }
    }

    private fun buildTitleAndExplanation(title: String, explanation: String): SpannedString {
        return buildSpannedString {
            inSpans(
                TextAppearanceSpan(
                    requireContext(),
                    R.style.TextAppearance_MaterialComponents_Body1
                )
            ) {
                append(title)
            }
            append("\n")
            inSpans(
                TextAppearanceSpan(
                    requireContext(),
                    R.style.TextAppearance_MaterialComponents_Body2
                ),
                ForegroundColorSpan(
                    ColorUtils.setAlphaComponent(
                        requireContext().getThemeColor(android.R.attr.textColorPrimary),
                        (255 * 0.6).toInt(),
                    )
                )
            ) {
                append(explanation)
            }
        }
    }
}
