package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.review.ExternalItemViewModel
import fr.jorisfavier.youshallnotpass.utils.CoroutineDispatchers
import fr.jorisfavier.youshallnotpass.utils.Event
import fr.jorisfavier.youshallnotpass.utils.State
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImportItemViewModel @Inject constructor(
    private val externalItemRepository: ExternalItemRepository,
    private val cryptoManager: CryptoManager,
    private val itemRepository: ItemRepository,
    private val dispatchers: CoroutineDispatchers,
) : ViewModel() {

    private val _navigate = MutableLiveData<Event<Unit>>()
    val navigate: LiveData<Event<Unit>> = _navigate

    private val _isSecureFile = MutableLiveData(false)
    val isSecureFile: LiveData<Boolean> = _isSecureFile

    private val _importedItems = MutableLiveData<List<ExternalItemViewModel>>(listOf())
    val importedItems: LiveData<List<ExternalItemViewModel>> = _importedItems

    private val _loadFromUriState = MutableLiveData<State<Unit>>()
    val loadFromUriState: LiveData<State<Unit>> = _loadFromUriState

    private val _importItemsState = MutableLiveData<State<Unit>>()
    val importItemsState: LiveData<State<Unit>> = _importItemsState


    val isFileSelected: Boolean
        get() = currentUri != null

    val isPasswordProvided: Boolean
        get() = !password.value.isNullOrEmpty()

    val isAtLeastOneItemSelected: Boolean
        get() = _importedItems.value.orEmpty().any { it.selected }

    val password = MutableLiveData<String>()

    private var currentUri: Uri? = null

    companion object {
        const val PASSWORD_NEEDED_SLIDE = 1
        const val REVIEW_ITEM_SLIDE = 2
        const val SUCCESS_FAIL_SLIDE = 3
    }

    fun setUri(uri: Uri) {
        viewModelScope.launch {
            currentUri = uri
            _isSecureFile.value =
                externalItemRepository.isSecuredWithPassword(uri).getOrDefault(false)
            _navigate.postValue(Event(Unit))
        }
    }

    fun onSlideChanged(position: Int) {
        Timber.d("Slide changed to $position")
        when (position) {
            PASSWORD_NEEDED_SLIDE -> {
                if (!_isSecureFile.value!!) {
                    _navigate.postValue(Event(Unit))
                }
            }

            REVIEW_ITEM_SLIDE -> {
                loadExternalItemsFromUri()
            }

            SUCCESS_FAIL_SLIDE -> {
                importItems()
            }
        }
    }

    fun selectAllItems() {
        _importedItems.value?.forEach { it.selected = true }
        _importedItems.postValue(_importedItems.value)
    }

    private fun loadExternalItemsFromUri() {
        viewModelScope.launch {
            _loadFromUriState.postValue(State.Loading)
            val uri = currentUri ?: return@launch
            externalItemRepository.getExternalItemsFromUri(uri, password.value)
                .onSuccess { items ->
                    _importedItems.postValue(items.map { ExternalItemViewModel(it, false) })
                    _loadFromUriState.postValue(State.Success(Unit))
                }
                .onFailure {
                    _loadFromUriState.postValue(State.Error)
                    _navigate.postValue(Event(Unit))
                }
        }
    }


    private fun importItems() {
        _importItemsState.value = State.Loading
        viewModelScope.launch {
            val itemsToImport = _importedItems.value
                ?.asFlow()
                ?.filter { it.selected }
                ?.map {
                    val externalItem = it.externalItem
                    val password = cryptoManager.encryptData(externalItem.password).getOrThrow()
                    Item(
                        0,
                        externalItem.title,
                        externalItem.login,
                        password.ciphertext,
                        password.initializationVector
                    )
                }
                ?.flowOn(dispatchers.io)
                ?.catch { emitAll(emptyFlow()) }
                ?.toList()
            if (!itemsToImport.isNullOrEmpty()) {
                itemRepository.insertItems(itemsToImport)
                _importItemsState.postValue(State.Success(Unit))
            } else {
                _importItemsState.postValue(State.Error)
            }
        }
    }

}
