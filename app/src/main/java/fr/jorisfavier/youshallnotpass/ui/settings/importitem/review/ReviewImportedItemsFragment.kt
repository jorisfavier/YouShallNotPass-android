package fr.jorisfavier.youshallnotpass.ui.settings.importitem.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.github.appintro.SlidePolicy
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentImportReviewItemBinding
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.toast
import javax.inject.Inject

class ReviewImportedItemsFragment : Fragment(R.layout.fragment_import_review_item), SlidePolicy {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ImportItemViewModel by activityViewModels { viewModelFactory }

    private val adapter = ImportedItemAdapter(listOf())
    private lateinit var binding: FragmentImportReviewItemBinding

    override val isPolicyRespected: Boolean
        get() = viewModel.isAtLeastOneItemSelected

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentImportReviewItemBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initObserver()
        binding.itemTotalCount = 0
        binding.importReviewSelectAll.setOnClickListener {
            viewModel.selectAllItems()
        }
    }

    override fun onUserIllegallyRequestedNextPage() {
        context?.toast(R.string.please_select_item)
    }

    private fun initObserver() {
        viewModel.importedItems.observe(viewLifecycleOwner) {
            binding.itemTotalCount = it.size
            adapter.updateData(it)
        }
        viewModel.loadFromUriState.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                binding.importReviewList.isVisible = state is State.Success
                binding.importReviewDescription.isVisible = state is State.Success
                binding.importReviewImage.isVisible = state is State.Success
                binding.importReviewSelectAll.isVisible = state is State.Success
                binding.importReviewItemCount.isVisible = state is State.Success
                binding.importReviewLoading.isVisible = state is State.Loading
            }
        }
    }

    private fun initList() {
        binding.importReviewList.adapter = adapter
        binding.importReviewList.setHasFixedSize(true)
    }

    companion object {
        fun newInstance(): ReviewImportedItemsFragment {
            return ReviewImportedItemsFragment()
        }
    }
}