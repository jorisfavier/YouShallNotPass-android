package fr.jorisfavier.youshallnotpass.ui.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentItemBinding
import fr.jorisfavier.youshallnotpass.model.exception.ItemAlreadyExistsException
import fr.jorisfavier.youshallnotpass.utils.toast
import kotlinx.android.synthetic.main.fragment_item.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ItemFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ItemEditViewModel by viewModels { viewModelFactory }
    private val args: ItemFragmentArgs by navArgs()

    private lateinit var binding: FragmentItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_item, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(if (args.itemId == 0) R.string.item_create_title else R.string.item_edit_title)
            show()
        }

        viewModel.initData(args.itemId)

        createOrUpdateItemButton.setOnClickListener {
            createOrUpdateItem()
        }

        generatePasswordButton.setOnClickListener {
            viewModel.generateSecurePassword()
        }

        passwordLengthBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                viewModel.setPasswordLength(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun createOrUpdateItem() {
        lifecycleScope.launchWhenStarted {
            viewModel.updateOrCreateItem().collect {
                val messageResourceId = when {
                    it.isSuccess -> it.getOrDefault(R.string.item_creation_success)
                    it.exceptionOrNull() is ItemAlreadyExistsException -> R.string.item_already_exist
                    else -> R.string.item_name_or_password_missing
                }
                context?.toast(messageResourceId)
                if (it.isSuccess) {
                    findNavController().popBackStack()
                }
            }
        }
    }
}
