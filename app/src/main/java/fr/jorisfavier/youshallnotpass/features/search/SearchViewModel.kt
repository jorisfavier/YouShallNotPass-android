package fr.jorisfavier.youshallnotpass.features.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.managers.IItemManager
import fr.jorisfavier.youshallnotpass.models.Item
import java.util.*

class SearchViewModel: ViewModel(){

    lateinit var itemManager: IItemManager

    private val _search =  MutableLiveData<String>()

    val results: LiveData<List<Item>> = Transformations.switchMap(_search) { query ->
        itemManager.searchItem(query)
    }

    fun setSearchValue(value: String){
        val input = value.toLowerCase(Locale.getDefault()).trim()
        //Prevent to search the same query twice
        if (input == _search.value) {
            return
        }
        _search.value = input
    }
}