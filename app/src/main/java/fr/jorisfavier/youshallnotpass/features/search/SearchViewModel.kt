package fr.jorisfavier.youshallnotpass.features.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.managers.IItemManager
import fr.jorisfavier.youshallnotpass.models.Item

class SearchViewModel : ViewModel() {

    lateinit var itemManager: IItemManager

    val search = MutableLiveData<String>()

    val results: LiveData<List<Item>> = Transformations.switchMap(search) { query ->
        itemManager.searchItem(query)
    }
    val hasNoResult: LiveData<Boolean> = Transformations.map(results) { listItem ->
        listItem.count() == 0
    }
}
