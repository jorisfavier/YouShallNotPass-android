package fr.jorisfavier.youshallnotpass.features.search

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.YSNPApplication
import fr.jorisfavier.youshallnotpass.databinding.ActivitySearchBinding
import fr.jorisfavier.youshallnotpass.managers.IItemManager
import fr.jorisfavier.youshallnotpass.models.Item
import kotlinx.android.synthetic.main.activity_search.*
import javax.inject.Inject

class SearchActivity: AppCompatActivity() {

    @Inject
    lateinit var itemManager: IItemManager

    private lateinit var viewmodel: SearchViewModel
    private lateinit var binding: ActivitySearchBinding
    private var searchAdapter: SearchResultAdapter = SearchResultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_search)
        YSNPApplication.currentInstance?.appComponent?.inject(this)
        viewmodel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        viewmodel.itemManager = itemManager
        initRecyclerView()
        binding.setLifecycleOwner(this)
        binding.viewModel = viewmodel
    }

    private fun initRecyclerView() {
        search_recyclerView.layoutManager = LinearLayoutManager(this)
        search_recyclerView.adapter = searchAdapter
        viewmodel.results.observe(this, Observer { result ->
            searchAdapter.updateResults(result)
        })
    }
}