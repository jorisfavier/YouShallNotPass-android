package fr.jorisfavier.youshallnotpass.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.utils.State

abstract class SearchBaseViewModel : ViewModel() {

    val search = MutableLiveData("")

    abstract val results: LiveData<State<List<Item>>>
    abstract val hasNoResult: LiveData<Boolean>
    abstract val noResultTextIdRes: LiveData<Int>

}
