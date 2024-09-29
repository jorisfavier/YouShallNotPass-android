package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.appintro.SlidePolicy
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentImportPasswordNeededBinding
import fr.jorisfavier.youshallnotpass.utils.autoCleared
import fr.jorisfavier.youshallnotpass.utils.extensions.hideKeyboard
import fr.jorisfavier.youshallnotpass.utils.extensions.toast

@AndroidEntryPoint
class ProvideImportPasswordFragment : Fragment(R.layout.fragment_import_password_needed),
    SlidePolicy {
    private var binding: FragmentImportPasswordNeededBinding by autoCleared()

    val viewModel: ImportItemViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImportPasswordNeededBinding.inflate(inflater, container, false)
        return binding.root
    }

    override val isPolicyRespected: Boolean
        get() {
            activity?.hideKeyboard()
            return viewModel.isPasswordProvided
        }

    override fun onUserIllegallyRequestedNextPage() {
        context?.toast(R.string.please_provide_password)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPasswordField()
        initObserver()
    }

    private fun initObserver() {
        viewModel.isSecureFile.observe(viewLifecycleOwner) {
            view?.isVisible = it
        }
    }

    private fun initPasswordField() {
        binding.password.doAfterTextChanged { text ->
            viewModel.onPasswordChanged(text.toString())
        }
    }

    companion object {
        fun newInstance(): ProvideImportPasswordFragment {
            return ProvideImportPasswordFragment()
        }
    }
}
