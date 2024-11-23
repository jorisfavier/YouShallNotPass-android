package fr.jorisfavier.youshallnotpass.ui.settings.importitem.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentImportReviewItemBinding
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.autoCleared

@AndroidEntryPoint
class ReviewImportedItemsFragment : Fragment(R.layout.fragment_import_review_item) {

    private val viewModel: ImportItemViewModel by activityViewModels()

    private val headerAdapter: ImportedItemHeaderAdapter by lazy {
        ImportedItemHeaderAdapter(onSelectAll = viewModel::selectAllItems)
    }

    private val adapter: ImportedItemAdapter by lazy {
        ImportedItemAdapter(
            onItemClicked = viewModel::selectItem,
        )
    }
    private var binding: FragmentImportReviewItemBinding by autoCleared()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImportReviewItemBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initObserver()
    }

    private fun initObserver() {
        viewModel.loadFromUriState.observe(viewLifecycleOwner) { state ->
            with(binding) {
                importReviewList.isVisible = state is State.Success
                importReviewLoading.isVisible = state is State.Loading
                if (state is State.Success) {
                    adapter.submitList(state.value)
                    headerAdapter.onItemCountChanged(state.value.size)
                }
            }
        }
    }

    private fun initList() {
        binding.importReviewList.adapter = ConcatAdapter(headerAdapter, adapter)
        binding.importReviewList.setHasFixedSize(true)
    }
}
