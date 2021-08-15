package fr.jorisfavier.youshallnotpass.ui.autofill

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.autofill.AutofillManager
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.ui.search.SearchBaseFragment
import fr.jorisfavier.youshallnotpass.utils.autofill.AutofillHelperCompat
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
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
            val direction = AutofillSearchFragmentDirections
                .actionAutofillSearchFragmentToItemFragment(
                    itemName = viewModel.appName.value?.peekContent(),
                )
            findNavController().navigate(direction)
        }
    }

    private fun initObservers() {
        viewModel.autofillResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { data ->
                val itemDataSet = AutofillHelperCompat.buildItemDataSet(
                    context = requireContext(),
                    fillRequest = data.fillRequest,
                    autofillItems = data.autofillItems,
                    item = data.item,
                    password = data.itemPassword,
                )
                requireActivity().apply {
                    setResult(
                        Activity.RESULT_OK, Intent().putExtra(
                            AutofillManager.EXTRA_AUTHENTICATION_RESULT,
                            itemDataSet,
                        )
                    )
                    finish()
                }
            }
        }
    }
}