package fr.jorisfavier.youshallnotpass.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.DialogSettingsExportBinding
import fr.jorisfavier.youshallnotpass.ui.home.HomeViewModel
import fr.jorisfavier.youshallnotpass.utils.autoCleared
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExportDialogFragment : DialogFragment(R.layout.dialog_settings_export) {

    private var binding: DialogSettingsExportBinding by autoCleared()

    val viewModel: SettingsViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogSettingsExportBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        with(binding) {
            cancel.setOnClickListener { dismiss() }
            export.setOnClickListener {
                onExportClicked()
            }
            radioGroup.setOnCheckedChangeListener { _, _ ->
                passwordContainer.isVisible = radioGroup.checkedRadioButtonId == R.id.ysnp_button
            }
        }
    }

    private fun onExportClicked() {
        with(binding) {
            val needPassword = radioGroup.checkedRadioButtonId == R.id.ysnp_button
            val password = if (needPassword) password.text?.toString()?.trim() else null
            if (needPassword && password.isNullOrEmpty()) {
                error.setText(R.string.export_password_missing)
                error.isVisible = true
                scrollview.post {
                    scrollview.fullScroll(View.FOCUS_DOWN)
                }
            } else {
                content.isInvisible = true
                progressIndicator.isVisible = true
                lifecycleScope.launch {
                    viewModel.exportPasswords(password).collectLatest { result ->
                        result
                            .onSuccess {
                                homeViewModel.ignoreNextPause()
                                requireActivity().startActivity(it)
                                dismiss()
                            }
                            .onFailure {
                                error.setText(R.string.password_export_failed)
                                error.isVisible = true
                                content.isVisible = true
                                progressIndicator.isVisible = false
                            }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "ExportDialogFragment"
    }
}
