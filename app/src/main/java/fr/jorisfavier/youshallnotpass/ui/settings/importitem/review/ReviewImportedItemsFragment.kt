package fr.jorisfavier.youshallnotpass.ui.settings.importitem.review

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.github.appintro.SlidePolicy
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.ImportItemViewModel
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.toast
import kotlinx.android.synthetic.main.fragment_import_review_item.*
import javax.inject.Inject

class ReviewImportedItemsFragment : Fragment(R.layout.fragment_import_review_item), SlidePolicy {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val viewModel: ImportItemViewModel by activityViewModels { viewModelFactory }

    private val adapter = ImportedItemAdapter(listOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override val isPolicyRespected: Boolean
        get() = viewModel.isAtLeastOneItemSelected

    override fun onUserIllegallyRequestedNextPage() {
        context?.toast(R.string.please_select_item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initObserver()
    }

    private fun initObserver() {
        viewModel.importedItems.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
        viewModel.loadFromUriState.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                importList.isVisible = state is State.Success
                importDescription.isVisible = state is State.Success
                importImage.isVisible = state is State.Success
                importListLoading.isVisible = state is State.Loading
            }
        }
    }

    private fun initList() {
        importList.adapter = adapter
    }

    companion object {
        fun newInstance(): ReviewImportedItemsFragment {
            return ReviewImportedItemsFragment()
        }
    }
}