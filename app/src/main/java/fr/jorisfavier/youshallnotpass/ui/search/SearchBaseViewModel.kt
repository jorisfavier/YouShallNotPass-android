package fr.jorisfavier.youshallnotpass.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.utils.State

abstract class SearchBaseViewModel : ViewModel() {

    protected val _search = MutableLiveData("")
    val search: LiveData<String> = _search

    abstract val results: LiveData<State<List<Item>>>
    abstract val hasNoResult: LiveData<Boolean>
    abstract val noResultTextIdRes: LiveData<Int>

    fun onSearchChanged(searchText: String) {
        _search.value = searchText
    }

}
