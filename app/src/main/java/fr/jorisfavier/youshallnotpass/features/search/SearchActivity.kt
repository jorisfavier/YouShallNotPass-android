package fr.jorisfavier.youshallnotpass.features.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders

import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.YSNPApplication
import fr.jorisfavier.youshallnotpass.managers.IItemManager
import javax.inject.Inject

class SearchActivity: AppCompatActivity() {

    @Inject
    lateinit var itemManager: IItemManager

    private lateinit var viewmodel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        YSNPApplication.currentInstance?.appComponent?.inject(this)
        viewmodel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        viewmodel.itemManager = itemManager
    }

    override fun onStart() {
        super.onStart()
    }
}