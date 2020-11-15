package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.getThemeColor
import kotlinx.android.synthetic.main.fragment_import_result.*
import javax.inject.Inject

class ImportResultFragment : Fragment(R.layout.fragment_import_result) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val viewModel: ImportItemViewModel by activityViewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
    }

    private fun initObserver() {
        viewModel.importItemsState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { state ->
                var descriptionTextId = R.string.import_failed
                var imageResId = R.drawable.key_ysnp_broken
                var colorResId = R.attr.colorError
                if (state is State.Success) {
                    descriptionTextId = R.string.import_completed
                    imageResId = R.drawable.ysnp_small
                    colorResId = R.attr.colorOnBackground
                }
                importImage.isVisible = state !is State.Loading
                importDescription.isVisible = state !is State.Loading
                importInProgress.isVisible = state is State.Loading
                importImage.setImageDrawable(AppCompatResources.getDrawable(requireContext(), imageResId))
                importDescription.text = getString(descriptionTextId)
                importDescription.setTextColor(requireContext().getThemeColor(colorResId))
            }
        }
    }

    companion object {
        fun newInstance(): ImportResultFragment {
            return ImportResultFragment()
        }
    }
}