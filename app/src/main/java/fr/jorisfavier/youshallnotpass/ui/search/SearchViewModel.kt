package fr.jorisfavier.youshallnotpass.ui.search

import android.content.ClipData
import android.content.ClipboardManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import fr.jorisfavier.youshallnotpass.repository.DesktopRepository
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.extensions.addFlagSensitiveData
import fr.jorisfavier.youshallnotpass.utils.extensions.combine
import fr.jorisfavier.youshallnotpass.utils.extensions.debounce
import fr.jorisfavier.youshallnotpass.utils.extensions.default
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class SearchViewModel(
    private val itemRepository: ItemRepository,
    private val cryptoManager: CryptoManager,
    private val clipboardManager: ClipboardManager,
    private val appPreference: AppPreferenceDataSource,
    private val desktopRepository: DesktopRepository,
    debounceDurationMs: Long,
) : SearchBaseViewModel() {

    @Inject
    constructor(
        itemRepository: ItemRepository,
        cryptoManager: CryptoManager,
        clipboardManager: ClipboardManager,
        appPreference: AppPreferenceDataSource,
        desktopRepository: DesktopRepository,
    ) : this(
        itemRepository,
        cryptoManager,
        clipboardManager,
        appPreference,
        desktopRepository,
        debounceDurationMs = 300,
    )


    @OptIn(ExperimentalCoroutinesApi::class)
    override val results = combine(
        search.asFlow(),
        appPreference.observeShouldHideItems(),
        itemRepository.getAllItems(),
    ) { query, hideAll, allItems -> Triple(query, hideAll, allItems) }
        .flatMapLatest { (query, hideAll, allItems) ->
            flow {
                emit(State.Loading)
                when {
                    query.isNotBlank() && query.isNotEmpty() -> {
                        emit(
                            State.Success(
                                itemRepository.searchItem("%$query%").getOrDefault(emptyList())
                            )
                        )
                    }

                    !hideAll -> {
                        emit(State.Success(allItems))
                    }

                    else -> {
                        emit(State.Success(listOf()))
                    }
                }
            }
        }
        .asLiveData(timeoutInMs = 0)
        .debounce(duration = debounceDurationMs, coroutineScope = viewModelScope)

    override val hasNoResult: LiveData<Boolean> = results.map { state ->
        state is State.Success && state.value.isEmpty()
    }

    override val noResultTextIdRes =
        search.combine(appPreference.observeShouldHideItems().asLiveData())
            .map { (search, hideAll) ->
                val isSearchEmpty = search.isEmpty() || search.isBlank()
                when {
                    isSearchEmpty && !hideAll -> R.string.no_item_yet
                    isSearchEmpty && hideAll -> R.string.use_the_search
                    else -> R.string.no_results_found
                }
            }.default(R.string.no_results_found)

    fun deleteItem(item: Item): Flow<Result<Unit>> {
        return flow {
            itemRepository.deleteItem(item)
            emit(Result.success(Unit))
        }.catch { e ->
            Timber.e(e, "Error during item deletion")
            emit(Result.failure(e))
        }
    }

    suspend fun decryptPassword(item: Item): Result<String> {
        return cryptoManager.decryptData(item.password, item.initializationVector)
    }


    fun copyToClipboard(item: Item, type: ItemDataType): Flow<Result<Int>> = flow {
        val (data, resId) = when (type) {
            ItemDataType.PASSWORD -> {
                val pwd = decryptPassword(item).getOrElse {
                    emit(Result.failure(it))
                    currentCoroutineContext().cancel()
                    return@flow
                }
                (pwd to R.string.copy_password_to_clipboard_success)
            }

            ItemDataType.LOGIN -> (item.login to R.string.copy_login_to_clipboard_success)
        }
        val clip = ClipData.newPlainText(type.name, data).apply {
            if (type == ItemDataType.PASSWORD) {
                addFlagSensitiveData()
            }
        }
        clipboardManager.setPrimaryClip(clip)
        emit(Result.success(resId))
    }

    fun sendToDesktop(item: Item, type: ItemDataType): Flow<Result<Int>> = flow {
        val data = when (type) {
            ItemDataType.PASSWORD -> decryptPassword(item).getOrElse {
                emit(Result.failure(YsnpException(R.string.error_occurred)))
                currentCoroutineContext().cancel()
                return@flow
            }

            ItemDataType.LOGIN -> item.login.orEmpty()
        }
        desktopRepository.sendData(data)
            .onSuccess {
                emit(Result.success(R.string.ysnp_desktop_communication_success))
            }
            .onFailure { error ->
                if (listOf(
                        HttpException::class,
                        NullPointerException::class,
                        ConnectException::class,
                    ).contains(error::class)
                ) {
                    emit(Result.failure(YsnpException(R.string.ysnp_desktop_communication_fail)))
                } else {
                    emit(Result.failure(error))
                }
            }
    }
}
