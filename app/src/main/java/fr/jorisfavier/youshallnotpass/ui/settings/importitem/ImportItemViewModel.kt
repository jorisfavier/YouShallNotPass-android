package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import fr.jorisfavier.youshallnotpass.repository.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.review.SelectableExternalItem
import fr.jorisfavier.youshallnotpass.utils.CoroutineDispatchers
import fr.jorisfavier.youshallnotpass.utils.Event
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.extensions.combine
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
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

    private val _navigate = MutableLiveData<Event<Int>>()
    val navigate: LiveData<Event<Int>> = _navigate

    private val isSecureFile = MutableLiveData(false)
    private val selectedItems = MutableLiveData<Set<ExternalItem>>(emptySet())
    private val password = MutableLiveData<String>()

    private val _loadFromUriState = MutableLiveData<State<List<SelectableExternalItem>>>()
    val loadFromUriState: LiveData<State<List<SelectableExternalItem>>> =
        _loadFromUriState.combine(selectedItems).map { (state, selectedItems) ->
            if (state is State.Success) {
                State.Success(
                    state.value.map {
                        SelectableExternalItem(
                            externalItem = it.externalItem,
                            isSelected = selectedItems.contains(it.externalItem),
                        )
                    }
                )
            } else {
                state
            }

        }

    private val _importItemsState = MutableLiveData<State<Unit>>()
    val importItemsState: LiveData<State<Unit>> = _importItemsState

    private val _error = MutableLiveData<Event<YsnpException>>()
    val error: LiveData<Event<YsnpException>> = _error

    private val isFileSelected: Boolean
        get() = currentUri != null

    private val isPasswordProvided: Boolean
        get() = !password.value.isNullOrEmpty()

    private val isAtLeastOneItemSelected: Boolean
        get() = selectedItems.value?.isNotEmpty() == true


    private var currentUri: Uri? = null
    private var currentStep: ImportItemStep = ImportItemStep.SELECT_FILE

    fun setUri(uri: Uri) {
        viewModelScope.launch {
            currentUri = uri
            isSecureFile.value =
                externalItemRepository.isSecuredWithPassword(uri).getOrDefault(false)
            _navigate.value = Event(ImportItemStep.PASSWORD_NEEDED.ordinal)
        }
    }

    fun onSlideChanged(position: Int) {
        Timber.d("Slide changed to $position")
        currentStep = ImportItemStep.entries[position]
        when (position) {
            ImportItemStep.PASSWORD_NEEDED.ordinal -> {
                if (!isSecureFile.value!!) {
                    _navigate.value = Event(ImportItemStep.REVIEW_ITEM.ordinal)
                }
            }

            ImportItemStep.REVIEW_ITEM.ordinal -> {
                loadExternalItemsFromUri()
            }

            ImportItemStep.SUCCESS_FAIL.ordinal -> {
                importItems()
            }
        }
    }

    fun onPasswordChanged(password: String) {
        this.password.value = password
    }

    fun selectItem(item: ExternalItem) {
        val selectedItems = selectedItems.value.orEmpty()
        val newList = if (selectedItems.contains(item)) {
            selectedItems - item
        } else {
            selectedItems + item
        }
        this.selectedItems.value = newList
    }

    fun selectAllItems() {
        selectedItems.value = (_loadFromUriState.value as? State.Success)?.value
            .orEmpty()
            .map { it.externalItem }
            .toSet()
    }

    fun goToNextStep() {
        when (currentStep) {
            ImportItemStep.SELECT_FILE -> {
                if (isFileSelected) {
                    _navigate.value = Event(ImportItemStep.PASSWORD_NEEDED.ordinal)
                } else {
                    _error.value = Event(YsnpException(R.string.please_select_file))
                }
            }

            ImportItemStep.PASSWORD_NEEDED -> {
                if (isPasswordProvided) {
                    _navigate.value = Event(ImportItemStep.REVIEW_ITEM.ordinal)
                } else {
                    _error.value = Event(YsnpException(R.string.please_provide_password))
                }
            }

            ImportItemStep.REVIEW_ITEM -> {
                if (isAtLeastOneItemSelected) {
                    _navigate.value = Event(ImportItemStep.SUCCESS_FAIL.ordinal)
                } else {
                    _error.value = Event(YsnpException(R.string.please_select_item))
                }
            }

            ImportItemStep.SUCCESS_FAIL -> {
                //nothing
            }
        }
    }

    private fun loadExternalItemsFromUri() {
        viewModelScope.launch {
            _loadFromUriState.postValue(State.Loading)
            val uri = currentUri ?: return@launch
            val items =
                externalItemRepository.getExternalItemsFromUri(uri, password.value).getOrNull()
            if (!items.isNullOrEmpty()) {
                _loadFromUriState.value = State.Success(
                    items.map { SelectableExternalItem(it, false) }
                )
            } else {
                _loadFromUriState.value = State.Error
                _navigate.value = Event(ImportItemStep.SUCCESS_FAIL.ordinal)
            }
        }
    }


    private fun importItems() {
        _importItemsState.value = State.Loading
        viewModelScope.launch {
            val itemsToImport = selectedItems.value.orEmpty()
                .asFlow()
                .map { externalItem ->
                    val password = cryptoManager.encryptData(externalItem.password).getOrThrow()
                    Item(
                        0,
                        externalItem.title,
                        externalItem.login,
                        password.ciphertext,
                        password.initializationVector
                    )
                }
                .flowOn(dispatchers.io)
                .catch { emitAll(emptyFlow()) }
                .toList()
            if (itemsToImport.isNotEmpty()) {
                itemRepository.insertItems(itemsToImport)
                    .onSuccess {
                        _importItemsState.value = State.Success(Unit)
                    }
                    .onFailure {
                        _importItemsState.value = State.Error
                    }
            } else {
                _importItemsState.value = State.Error
            }
        }
    }

}
