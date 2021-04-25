package fr.jorisfavier.youshallnotpass.ui.autofill

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentAutofillSearchBinding
import fr.jorisfavier.youshallnotpass.utils.autoCleared
import javax.inject.Inject

class AutofillSearchFragment : Fragment(R.layout.fragment_autofill_search) {

    private var binding: FragmentAutofillSearchBinding by autoCleared()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: AutofillViewModel by activityViewModels { viewModelFactory }

    private val searchAdapter by lazy {
        AutofillAdapter(viewModel::onItemClicked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAutofillSearchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()
        initRecyclerView()
        initNewButton()
        initObservers()
    }

    private fun initRecyclerView() {
        with(binding.searchRecyclerview) {
            adapter = searchAdapter
        }
    }

    private fun initNewButton() {
        binding.searchAddNewItemButton.setOnClickListener {
            val direction =
                AutofillSearchFragmentDirections.actionAutofillSearchFragmentToItemFragment()
            findNavController().navigate(direction)
        }
    }

    private fun initObservers() {
        viewModel.results.observe(viewLifecycleOwner, searchAdapter::submitList)

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