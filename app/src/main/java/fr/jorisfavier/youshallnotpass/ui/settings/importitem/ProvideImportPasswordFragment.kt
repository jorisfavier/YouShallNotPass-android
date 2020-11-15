package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.github.appintro.SlidePolicy
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.utils.hideKeyboard
import fr.jorisfavier.youshallnotpass.utils.toast
import kotlinx.android.synthetic.main.fragment_import_password_needed.*
import javax.inject.Inject

class ProvideImportPasswordFragment : Fragment(R.layout.fragment_import_password_needed), SlidePolicy {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val viewModel: ImportItemViewModel by activityViewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
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
        initPasswordEditText()
        initObserver()
    }

    private fun initObserver() {
        viewModel.isSecureFile.observe(viewLifecycleOwner) {
            view?.isVisible = it
        }
    }

    private fun initPasswordEditText() {
        importPasswordEditText.setText(viewModel.password)
        importPasswordEditText.doOnTextChanged { text, _, _, _ -> viewModel.password = text.toString() }
    }

    companion object {
        fun newInstance(): ProvideImportPasswordFragment {
            return ProvideImportPasswordFragment()
        }
    }
}