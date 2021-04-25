package fr.jorisfavier.youshallnotpass.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentSearchBinding
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.utils.autoCleared
import jp.wasabeef.recyclerview.animators.FadeInRightAnimator

abstract class SearchBaseFragment : Fragment() {

    protected var binding: FragmentSearchBinding by autoCleared()

    abstract val viewModel: SearchBaseViewModel

    abstract val searchAdapter: ListAdapter<Item, RecyclerView.ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initSettings()
        binding.searchAddNewItemButton.setOnClickListener {
            navigateToCreateNewItem()
        }
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshItems()
    }

    private fun initSettings() {
        binding.searchSettingsButton.setOnClickListener {
            val action = SearchFragmentDirections.actionSearchFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
    }

    private fun initRecyclerView() {
        binding.searchRecyclerview.apply {
            adapter = searchAdapter
            setHasFixedSize(true)
            itemAnimator = FadeInRightAnimator()
        }
        viewModel.results.observe(viewLifecycleOwner) { result ->
            searchAdapter.submitList(result)
        }
    }

    private fun navigateToCreateNewItem() {
        val action = SearchFragmentDirections.actionSearchFragmentToItemFragment()
        findNavController().navigate(action)
    }
}
