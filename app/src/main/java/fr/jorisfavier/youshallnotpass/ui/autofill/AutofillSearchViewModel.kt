package fr.jorisfavier.youshallnotpass.ui.autofill

import android.app.assist.AssistStructure
import android.content.Intent
import android.content.pm.PackageManager
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
import fr.jorisfavier.youshallnotpass.utils.Event
import fr.jorisfavier.youshallnotpass.utils.autofill.AutofillHelper26
import fr.jorisfavier.youshallnotpass.utils.extensions.combine
import fr.jorisfavier.youshallnotpass.utils.extensions.default
import fr.jorisfavier.youshallnotpass.utils.extensions.getDomainIfUrl
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class AutofillSearchViewModel @Inject constructor(
    private val itemRepository: IItemRepository,
    private val cryptoManager: ICryptoManager,
    private val packageManager: PackageManager,
) : SearchBaseViewModel() {

    private val _autofillResponse = MutableLiveData<Event<Intent>>()
    val autofillResponse: LiveData<Event<Intent>> = _autofillResponse

    private val autofillParsedStructure = MutableLiveData<AutofillParsedStructure>()
    val appName = autofillParsedStructure.map { Event(it.appName) }

    override val results = search.combine(autofillParsedStructure)
        .switchMap { (query, parsedStructure) ->
            liveData<List<Item>> {
                try {
                    when {
                        query.isNotBlank() && query.isNotEmpty() -> {
                            emit(itemRepository.searchItem("%$query%"))
                        }
                        parsedStructure.certificatesHashes.isNotEmpty() -> {
                            val byCertificatesResult =
                                itemRepository.searchItemByCertificates(parsedStructure.certificatesHashes)
                            if (byCertificatesResult.isNotEmpty()) {
                                emit(byCertificatesResult)
                            } else {
                                search.value = parsedStructure.webDomain?.getDomainIfUrl()
                                    ?: parsedStructure.appName
                            }
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

    fun setAssistStructure(assistStructure: AssistStructure) {
        autofillParsedStructure.value =
            AssistStructureUtil.traverseStructure(assistStructure, packageManager)
    }

    fun onItemClicked(item: Item) {
        viewModelScope.launch {
            val parsedStructure = autofillParsedStructure.value ?: return@launch
            if (parsedStructure.certificatesHashes.isNotEmpty()) {
                val updatedItem = item.copy(packageCertificate = parsedStructure.certificatesHashes)
                itemRepository.updateOrCreateItem(updatedItem)
            }
            val itemPassword = cryptoManager.decryptData(item.password, item.initializationVector)
            val data = AutofillHelper26.buildItemDataSet(
                autofillItems = parsedStructure.items,
                item = item,
                password = itemPassword,
            )
            _autofillResponse.value =
                Event(Intent().putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, data))
        }
    }

}