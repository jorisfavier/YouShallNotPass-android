package fr.jorisfavier.youshallnotpass.ui.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentItemBinding
import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import fr.jorisfavier.youshallnotpass.utils.autoCleared
import fr.jorisfavier.youshallnotpass.utils.extensions.toast
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class ItemFragment : Fragment(R.layout.fragment_item) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ItemEditViewModel by viewModels { viewModelFactory }
    private val args: ItemFragmentArgs by navArgs()

    private var binding: FragmentItemBinding by autoCleared()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentItemBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title =
                getString(if (args.itemId == 0) R.string.item_create_title else R.string.item_edit_title)
            show()
        }

        viewModel.initData(args.itemId, args.itemName)

        binding.itemviewCreateOrUpdateItemButton.setOnClickListener {
            createOrUpdateItem()
        }

        binding.itemviewGeneratePasswordButton.setOnClickListener {
            viewModel.generateSecurePassword()
        }
    }

    private fun createOrUpdateItem() {
        lifecycleScope.launchWhenStarted {
            viewModel.updateOrCreateItem().collect {
                val messageResourceId = when {
                    it.isSuccess -> it.getOrDefault(R.string.item_creation_success)
                    it.isFailure -> (it.exceptionOrNull() as? YsnpException)?.messageResId
                        ?: R.string.error_occurred
                    else -> R.string.error_occurred
                }
                context?.toast(messageResourceId)
                if (it.isSuccess) {
                    findNavController().popBackStack()
                }
            }
        }
    }
}
