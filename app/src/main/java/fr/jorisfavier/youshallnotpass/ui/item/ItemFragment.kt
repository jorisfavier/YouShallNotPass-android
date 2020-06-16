package fr.jorisfavier.youshallnotpass.ui.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentItemBinding
import kotlinx.android.synthetic.main.fragment_item.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ItemFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ItemEditViewModel by viewModels { viewModelFactory }

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
        (activity as AppCompatActivity).supportActionBar?.show()

        createItemButton.setOnClickListener {
            createNewItem()
        }

        generatePasswordButton.setOnClickListener {
            viewModel.generateSecurePassword()
        }
    }

    private fun createNewItem() {
        lifecycleScope.launch {
            viewModel.addNewItem().collect {
                if (it.isSuccess) {
                    Toast.makeText(
                            context,
                            getString(R.string.item_creation_success),
                            Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                            context,
                            getString(R.string.item_name_or_password_missing),
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
