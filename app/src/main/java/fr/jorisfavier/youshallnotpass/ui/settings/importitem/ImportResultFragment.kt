package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentImportResultBinding
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.autoCleared
import fr.jorisfavier.youshallnotpass.utils.extensions.getThemeColor

@AndroidEntryPoint
class ImportResultFragment : Fragment(R.layout.fragment_import_result) {

    private val viewModel: ImportItemViewModel by activityViewModels()
    private var binding: FragmentImportResultBinding by autoCleared()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImportResultBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
    }

    private fun initObserver() {
        viewModel.importItemsState.observe(viewLifecycleOwner) { state ->
            var descriptionTextId = R.string.import_failed
            var imageResId = R.drawable.key_ysnp_broken
            var colorResId = R.attr.colorError
            if (state is State.Success) {
                descriptionTextId = R.string.import_completed
                imageResId = R.drawable.ysnp_small
                colorResId = R.attr.colorOnBackground
            }
            binding.importResultImage.isVisible = state !is State.Loading
            binding.importResultDescription.isVisible = state !is State.Loading
            binding.importResultProgress.isVisible = state is State.Loading
            binding.importResultImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(),
                    imageResId
                )
            )
            binding.importResultDescription.text = getString(descriptionTextId)
            binding.importResultDescription.setTextColor(
                requireContext().getThemeColor(
                    colorResId
                )
            )
        }
    }
}
