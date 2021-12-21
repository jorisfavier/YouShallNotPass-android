package fr.jorisfavier.youshallnotpass.ui.search

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.ui.settings.SettingsFragment
import fr.jorisfavier.youshallnotpass.utils.extensions.onUnknownFailure
import fr.jorisfavier.youshallnotpass.utils.extensions.onYsnpFailure
import fr.jorisfavier.youshallnotpass.utils.extensions.toast
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SearchFragment : SearchBaseFragment() {

    override val viewModel: SearchViewModel by viewModels()

    override val searchAdapter by lazy {
        SearchResultAdapter(
            this::navigateToEditItemFragment,
            this::deleteItem,
            viewModel::decryptPassword,
            this::copyToClipboard,
            this::copyToDesktop
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchAddNewItemButton.setOnClickListener {
            navigateToCreateNewItem()
        }
    }

    private fun navigateToCreateNewItem() {
        val action = SearchFragmentDirections.actionSearchFragmentToItemFragment()
        findNavController().navigate(action)
    }

    private fun navigateToEditItemFragment(item: Item) {
        val action = SearchFragmentDirections.actionSearchFragmentToItemFragment(item.id)
        findNavController().navigate(action)
    }

    private fun deleteItem(item: Item) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_confirmation)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                lifecycleScope.launchWhenStarted {
                    viewModel.deleteItem(item).collect {
                        var message = R.string.error_occurred
                        if (it.isSuccess) {
                            message = R.string.delete_success
                        }
                        Toast.makeText(context, getString(message), Toast.LENGTH_LONG).show()
                    }
                }
            }
            .show()
    }

    private fun copyToClipboard(item: Item, type: ItemDataType) {
        viewModel.copyToClipboard(item, type)
            .onSuccess { context?.toast(it) }
            .onFailure { context?.toast(R.string.error_occurred) }
    }

    private fun copyToDesktop(item: Item, type: ItemDataType) {
        lifecycleScope.launchWhenCreated {
            viewModel.sendToDesktop(item, type).collect { result ->
                result
                    .onSuccess { requireContext().toast(it) }
                    .onYsnpFailure {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.error_occurred)
                            .setMessage(it.messageResId)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(R.string.sync_desktop_amd_mobile) { _, _ ->
                                val direction =
                                    SearchFragmentDirections.actionSearchFragmentToSettingsFragment(
                                        highlightItem = SettingsFragment.KEY_DESKTOP
                                    )
                                findNavController().navigate(direction)
                            }
                            .show()
                    }
                    .onUnknownFailure { requireContext().toast(R.string.error_desktop_transmission) }
            }
        }
    }
}
