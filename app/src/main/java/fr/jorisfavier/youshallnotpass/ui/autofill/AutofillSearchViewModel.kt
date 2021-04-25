package fr.jorisfavier.youshallnotpass.ui.autofill

import android.app.assist.AssistStructure
import android.content.Intent
import android.os.Build
import android.view.autofill.AutofillManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.AutofillParsedStructure
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.ui.search.SearchBaseViewModel
import fr.jorisfavier.youshallnotpass.utils.AssistStructureUtil
import fr.jorisfavier.youshallnotpass.utils.AutofillHelper
import fr.jorisfavier.youshallnotpass.utils.Event
import fr.jorisfavier.youshallnotpass.utils.extensions.default
import timber.log.Timber
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class AutofillSearchViewModel @Inject constructor(
    itemRepository: IItemRepository,
    private val cryptoManager: ICryptoManager,
) : SearchBaseViewModel() {

    private val _autofillResponse = MutableLiveData<Event<Intent>>()
    val autofillResponse: LiveData<Event<Intent>> = _autofillResponse

    private val autofillNodes = mutableListOf<AutofillParsedStructure>()

    override val results = search.switchMap { query ->
        liveData<List<Item>> {
            try {
                when {
                    query.isNotBlank() && query.isNotEmpty() -> {
                        emit(itemRepository.searchItem("%$query%"))
                    }
                    else -> {
                        emit(listOf())
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error while searching for items")
                emit(listOf())
            }
        }
    }

    override val hasNoResult: LiveData<Boolean> = results.map { listItem ->
        listItem.count() == 0
    }

    override val noResultTextIdRes = search.switchMap { search ->
        liveData {
            val isSearchEmpty = search.isEmpty() || search.isBlank()
            if (isSearchEmpty) emit(R.string.no_results_found)
        }
    }.default(R.string.no_results_found)

    fun setAssistStructure(assistStructure: AssistStructure?) {
        if (assistStructure != null) {
            autofillNodes.clear()
            autofillNodes.addAll(AssistStructureUtil.traverseStructure(assistStructure))
        }
    }

    fun onItemClicked(item: Item) {
        val itemPassword = cryptoManager.decryptData(item.password, item.initializationVector)
        val data = AutofillHelper.buildDataSet(
            parsedStructure = autofillNodes,
            item = item,
            password = itemPassword,
        )
        _autofillResponse.value =
            Event(Intent().putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, data))
    }

}