package fr.jorisfavier.youshallnotpass.features.search

import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.managers.IItemManager

class SearchViewModel: ViewModel(){

    lateinit var itemManager: IItemManager
    var search: String = ""
}