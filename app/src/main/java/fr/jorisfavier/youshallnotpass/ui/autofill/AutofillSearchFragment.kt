package fr.jorisfavier.youshallnotpass.ui.autofill

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.ui.search.SearchBaseFragment
import javax.inject.Inject

class AutofillSearchFragment : SearchBaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val viewModel: AutofillSearchViewModel by activityViewModels { viewModelFactory }

    override val searchAdapter by lazy {
        AutofillAdapter(viewModel::onItemClicked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchSettingsButton.isInvisible = true
        initNewButton()
        initObservers()
    }

    private fun initNewButton() {
        binding.searchAddNewItemButton.setOnClickListener {
            val direction =
                AutofillSearchFragmentDirections.actionAutofillSearchFragmentToItemFragment()
            findNavController().navigate(direction)
        }
    }

    private fun initObservers() {
        viewModel.autofillResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { intent ->
                requireActivity().apply {
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }
}