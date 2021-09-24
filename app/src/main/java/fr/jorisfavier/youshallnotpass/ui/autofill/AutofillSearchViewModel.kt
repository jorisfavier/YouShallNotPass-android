package fr.jorisfavier.youshallnotpass.ui.autofill

import android.app.assist.AssistStructure
import android.content.pm.PackageManager
import android.os.Build
import android.service.autofill.FillRequest
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.AutofillParsedStructure
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.search.SearchBaseViewModel
import fr.jorisfavier.youshallnotpass.utils.AssistStructureUtil
import fr.jorisfavier.youshallnotpass.utils.Event
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.extensions.combine
import fr.jorisfavier.youshallnotpass.utils.extensions.default
import fr.jorisfavier.youshallnotpass.utils.extensions.getDomainIfUrl
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class AutofillSearchViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val cryptoManager: CryptoManager,
    private val packageManager: PackageManager,
) : SearchBaseViewModel() {

    private var firstSearchProcessed = false
    private lateinit var fillRequest: FillRequest

    private val _autofillResponse = MutableLiveData<Event<AutofillDataSetInfo>>()
    val autofillResponse: LiveData<Event<AutofillDataSetInfo>> = _autofillResponse

    private val autofillParsedStructure = MutableLiveData<AutofillParsedStructure>()
    val appName = autofillParsedStructure.map { Event(it.appName) }

    override val results = search.combine(autofillParsedStructure)
        .switchMap { (query, parsedStructure) ->
            liveData<State<List<Item>>> {
                try {
                    when {
                        query.isNotBlank() && query.isNotEmpty() -> {
                            emit(State.Success(itemRepository.searchItem("%$query%")))
                        }
                        parsedStructure.certificatesHashes.isNotEmpty() -> {
                            val byCertificatesResult =
                                itemRepository.searchItemByCertificates(parsedStructure.certificatesHashes)
                            if (byCertificatesResult.isNotEmpty()) {
                                emit(State.Success(byCertificatesResult))
                            } else if (!firstSearchProcessed) {
                                //When landing to this fragment the first time
                                //if we haven't found any result from the certificate
                                //we try to find some items from the app name or web domain
                                search.value = parsedStructure.webDomain?.getDomainIfUrl()
                                    ?: parsedStructure.appName
                                firstSearchProcessed = true
                            }
                        }
                        else -> {
                            emit(State.Success(listOf()))
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error while searching for items")
                    emit(State.Success(listOf()))
                }
            }
        }

    override val hasNoResult: LiveData<Boolean> = results.map { state ->
        state is State.Success && state.value.count() == 0
    }

    override val noResultTextIdRes = search.switchMap { search ->
        liveData {
            val isSearchEmpty = search.isEmpty() || search.isBlank()
            if (isSearchEmpty) emit(R.string.no_results_found)
        }
    }.default(R.string.no_results_found)

    fun setAutofillInfos(assistStructure: AssistStructure, fillRequest: FillRequest) {
        autofillParsedStructure.value =
            AssistStructureUtil.traverseStructure(assistStructure, packageManager)
        this.fillRequest = fillRequest
    }

    fun onItemClicked(item: Item) {
        viewModelScope.launch {
            val parsedStructure = autofillParsedStructure.value ?: return@launch
            if (parsedStructure.certificatesHashes.isNotEmpty()) {
                val updatedItem = item.copy(packageCertificate = parsedStructure.certificatesHashes)
                runCatching { itemRepository.updateOrCreateItem(updatedItem) }
            }
            val itemPassword = cryptoManager.decryptData(item.password, item.initializationVector)
            val data = AutofillDataSetInfo(
                autofillItems = parsedStructure.items,
                item = item,
                itemPassword = itemPassword,
                fillRequest = fillRequest,
            )
            _autofillResponse.value = Event(data)
        }
    }

}