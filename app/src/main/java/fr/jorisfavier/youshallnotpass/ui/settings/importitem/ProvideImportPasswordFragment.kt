package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentImportPasswordNeededBinding
import fr.jorisfavier.youshallnotpass.utils.autoCleared

@AndroidEntryPoint
class ProvideImportPasswordFragment : Fragment(R.layout.fragment_import_password_needed) {
    private var binding: FragmentImportPasswordNeededBinding by autoCleared()

    val viewModel: ImportItemViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImportPasswordNeededBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPasswordField()
    }

    private fun initPasswordField() {
        binding.password.doAfterTextChanged { text ->
            viewModel.onPasswordChanged(text.toString())
        }
    }
}
