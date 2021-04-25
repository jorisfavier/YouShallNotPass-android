package fr.jorisfavier.youshallnotpass.ui.autofill

import android.app.assist.AssistStructure
import android.content.Intent
import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import fr.jorisfavier.youshallnotpass.BuildConfig
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.AutofillItemType
import fr.jorisfavier.youshallnotpass.model.AutofillParsedStructure
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.utils.AssistStructureUtil
import fr.jorisfavier.youshallnotpass.utils.Event
import timber.log.Timber
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class AutofillViewModel @Inject constructor(
    itemRepository: IItemRepository,
    private val cryptoManager: ICryptoManager,
) : ViewModel() {

    val search = MutableLiveData("")

    private val _autofillResponse = MutableLiveData<Event<Intent>>()
    val autofillResponse: LiveData<Event<Intent>> = _autofillResponse

    private val autofillNodes = mutableListOf<AutofillParsedStructure>()

    val results = search.switchMap { query ->
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

    val hasNoResult: LiveData<Boolean> = results.map { listItem ->
        listItem.count() == 0
    }

    fun setAssistStructure(assistStructure: AssistStructure?) {
        if (assistStructure != null) {
            autofillNodes.clear()
            autofillNodes.addAll(AssistStructureUtil.traverseStructure(assistStructure))
        }
    }

    fun onItemClicked(item: Item) {
        val dataSet = Dataset.Builder()
        val itemPassword = cryptoManager.decryptData(item.password, item.initializationVector)
        autofillNodes.forEach {
            when (it.type) {
                AutofillItemType.LOGIN -> {
                    dataSet.setValue(
                        it.id,
                        AutofillValue.forText(item.login),
                        RemoteViews(
                            BuildConfig.APPLICATION_ID,
                            R.layout.autofill_response
                        ).apply {
                            setTextViewText(R.id.title, item.title)
                            setTextViewText(R.id.subtitle, item.login)
                        }
                    )
                }
                AutofillItemType.PASSWORD -> {
                    dataSet.setValue(
                        it.id,
                        AutofillValue.forText(itemPassword),
                        RemoteViews(
                            BuildConfig.APPLICATION_ID,
                            R.layout.autofill_response
                        ).apply {
                            setTextViewText(R.id.title, item.title)
                            setTextViewText(R.id.subtitle, item.login)
                        }
                    )
                }
            }
        }
        val data = dataSet.build()
        val fillResponse = FillResponse.Builder()
            .addDataset(data)
            .build()
        _autofillResponse.value =
            Event(Intent().putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, data))
    }

}