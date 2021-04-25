package fr.jorisfavier.youshallnotpass.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.model.Item

abstract class SearchBaseViewModel : ViewModel() {

    val search = MutableLiveData("")

    abstract val results: LiveData<List<Item>>
    abstract val hasNoResult: LiveData<Boolean>
    abstract val noResultTextIdRes: LiveData<Int>

    fun refreshItems() {
        search.value = search.value
    }

}