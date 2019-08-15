package fr.jorisfavier.youshallnotpass.features.search

import androidx.lifecycle.*
import fr.jorisfavier.youshallnotpass.managers.IItemManager
import fr.jorisfavier.youshallnotpass.models.Item
import java.util.*

class SearchViewModel: ViewModel(){

    lateinit var itemManager: IItemManager

    private val mediator = MediatorLiveData<String>()
    val search =  MutableLiveData<String>()


    val results: LiveData<List<Item>> = Transformations.switchMap(mediator) { query ->
        itemManager.searchItem(query)
    }
    val hasNoResult: LiveData<Boolean> = Transformations.map(results) { listItem ->
        listItem.count() == 0
    }

    init {
        mediator.addSource(search) { value ->
            val input = value.toLowerCase(Locale.getDefault()).trim()
            //Prevent to search the same query twice
            if (input != search.value) {
                mediator.value = value
            }
        }
    }
}