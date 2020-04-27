package fr.jorisfavier.youshallnotpass.features.item

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.ItemEditFragmentBinding


class ItemEditFragment : Fragment() {

    companion object {
        fun newInstance() = ItemEditFragment()
        val editItemFragmentTag = "editItemFragmentId"
    }

    private lateinit var viewModel: ItemEditViewModel
    private lateinit var binding: ItemEditFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.item_edit_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ItemEditViewModel::class.java)

    }

}
