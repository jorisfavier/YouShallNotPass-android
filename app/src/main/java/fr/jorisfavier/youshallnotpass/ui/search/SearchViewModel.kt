package fr.jorisfavier.youshallnotpass.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.data.models.Item

class SearchViewModel : ViewModel() {

    lateinit var itemRepository: IItemRepository

    val search = MutableLiveData<String>()

    val results: LiveData<List<Item>> = Transformations.switchMap(search) { query ->
        itemRepository.searchItem(query)
    }
    val hasNoResult: LiveData<Boolean> = Transformations.map(results) { listItem ->
        listItem.count() == 0
    }
}
