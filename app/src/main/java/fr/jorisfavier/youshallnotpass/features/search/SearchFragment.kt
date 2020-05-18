package fr.jorisfavier.youshallnotpass.features.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.YSNPApplication
import fr.jorisfavier.youshallnotpass.databinding.FragmentSearchBinding
import fr.jorisfavier.youshallnotpass.managers.IItemManager
import kotlinx.android.synthetic.main.fragment_search.*
import javax.inject.Inject

class SearchFragment : Fragment() {

    @Inject
    lateinit var itemManager: IItemManager
    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewmodel: SearchViewModel
    private var searchAdapter: SearchResultAdapter = SearchResultAdapter()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        YSNPApplication.currentInstance?.appComponent?.inject(this)
        viewmodel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        viewmodel.itemManager = itemManager
        binding.lifecycleOwner = this
        binding.viewModel = viewmodel
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        search_add_btn.setOnClickListener {
            addNewItem(it)
        }
    }

    private fun initRecyclerView() {
        search_recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        search_recyclerView.adapter = searchAdapter
        viewmodel.results.observe(viewLifecycleOwner, Observer { result ->
            searchAdapter.updateResults(result)
        })
    }

    fun addNewItem(view: View) {
        val action = SearchFragmentDirections.actionSearchFragmentToItemFragment()
        view.findNavController().navigate(action)
    }
}
