package fr.jorisfavier.youshallnotpass.ui.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentItemBinding
import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import fr.jorisfavier.youshallnotpass.utils.autoCleared
import fr.jorisfavier.youshallnotpass.utils.extensions.doOnProgressChanged
import fr.jorisfavier.youshallnotpass.utils.extensions.toast
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ItemFragment : Fragment(R.layout.fragment_item) {

    private val viewModel: ItemEditViewModel by viewModels()
    private val args: ItemFragmentArgs by navArgs()

    private var binding: FragmentItemBinding by autoCleared()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title =
                getString(if (args.itemId == 0) R.string.item_create_title else R.string.item_edit_title)
            show()
        }
        initUI()
        initObservers()
        viewModel.initData(args.itemId, args.itemName)
    }

    private fun initUI() {
        with(binding) {
            passwordLengthBar.doOnProgressChanged { length ->
                viewModel.onPasswordLengthChanged(length)
            }
            createOrUpdateItemButton.setOnClickListener {
                password.text?.toString()
                createOrUpdateItem(
                    name = name.text?.toString(),
                    password = password.text?.toString(),
                    login = login.text?.toString(),
                )
            }
            generatePasswordButton.setOnClickListener {
                password.setText(
                    viewModel.generateSecurePassword(
                        hasUppercase = uppercaseCheckbox.isChecked,
                        hasNumber = numberCheckbox.isChecked,
                        hasSymbol = symbolCheckbox.isChecked,
                    )
                )
            }
        }
    }

    private fun initObservers() {
        viewModel.currentItem.observe(viewLifecycleOwner) { item ->
            with(binding) {
                name.setText(item?.title)
                login.setText(item?.login)
            }
        }
        viewModel.password.observe(viewLifecycleOwner) { password ->
            binding.password.setText(password)
        }
        viewModel.passwordLength.observe(viewLifecycleOwner) { length ->
            binding.passwordLengthValue.text = length.toString()
        }
        viewModel.createOrUpdateText.observe(viewLifecycleOwner) { textResId ->
            binding.createOrUpdateItemButton.setText(textResId)
        }
    }

    private fun createOrUpdateItem(
        name: String?,
        password: String?,
        login: String?,
    ) {
        lifecycleScope.launch {
            viewModel.updateOrCreateItem(
                name = name,
                password = password,
                login = login,
            ).collect {
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
