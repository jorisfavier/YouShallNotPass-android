package fr.jorisfavier.youshallnotpass.ui.search

import androidx.lifecycle.*
import fr.jorisfavier.youshallnotpass.data.model.Item
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import javax.inject.Inject

class SearchViewModel @Inject constructor(private val itemRepository: IItemRepository) : ViewModel() {

    val search = MutableLiveData<String>()

    val results: LiveData<List<Item>> = Transformations.switchMap(search) { query ->
        liveData {
            if (query.isNotBlank() && query.isNotEmpty()) {
                emit(itemRepository.searchItem("$query%"))
            } else {
                emit(listOf())
            }
        }
    }
    val hasNoResult: LiveData<Boolean> = Transformations.map(results) { listItem ->
        listItem.count() == 0
    }
}
