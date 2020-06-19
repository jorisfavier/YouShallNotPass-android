package fr.jorisfavier.youshallnotpass.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.data.model.Item
import fr.jorisfavier.youshallnotpass.databinding.FragmentSearchBinding
import kotlinx.android.synthetic.main.fragment_search.*
import javax.inject.Inject

class SearchFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentSearchBinding
    private var searchAdapter: SearchResultAdapter =
            SearchResultAdapter(this::navigateToEditItemFragment)

    private val viewModel: SearchViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        addNewItemButton.setOnClickListener {
            addNewItem(it)
        }
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    private fun initRecyclerView() {
        searchRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        searchRecyclerView.adapter = searchAdapter
        viewModel.results.observe(viewLifecycleOwner, Observer { result ->
            searchAdapter.updateResults(result)
        })
    }

    private fun addNewItem(view: View) {
        val action = SearchFragmentDirections.actionSearchFragmentToItemFragment()
        view.findNavController().navigate(action)
    }

    private fun navigateToEditItemFragment(item: Item) {
        val action = SearchFragmentDirections.actionSearchFragmentToItemFragment(item.id)
        findNavController().navigate(action)
    }
}
