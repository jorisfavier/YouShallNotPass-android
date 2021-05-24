package fr.jorisfavier.youshallnotpass.service

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.service.autofill.*
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import dagger.android.AndroidInjection
import fr.jorisfavier.youshallnotpass.manager.IAuthManager
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.AutofillParsedStructure
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.ui.autofill.AutofillActivity
import fr.jorisfavier.youshallnotpass.utils.AssistStructureUtil
import fr.jorisfavier.youshallnotpass.utils.AutofillHelper
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class YsnpAutofillService : AutofillService() {

    @Inject
    lateinit var itemRepository: IItemRepository

    @Inject
    lateinit var authManager: IAuthManager

    @Inject
    lateinit var cryptoManager: ICryptoManager

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val context: List<FillContext> = request.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure
        val parsedStructure = AssistStructureUtil.traverseStructure(structure, packageManager)
        val fillResponse =
            when {
                parsedStructure.isNewCredentials -> {
                    buildSuggestCredentialsResponse(
                        parsedStructure = parsedStructure,
                        clientState = request.clientState ?: bundleOf(),
                    )
                }
                parsedStructure.items.isEmpty() -> {
                    null
                }
                parsedStructure.webDomain != null || !authManager.isAutofillUnlocked -> {
                    buildRequiresAuthResponse(parsedStructure)
                }
                else -> {
                    buildAutofillResponse(parsedStructure)
                }
            }
        fillResponse?.setIgnoredIds(*parsedStructure.ignoreIds.toTypedArray())
        callback.onSuccess(fillResponse?.build())
    }

    override fun onSaveRequest(saveRequest: SaveRequest, saveCallback: SaveCallback) {
        val context: List<FillContext> = saveRequest.fillContexts
        val parsedStructure = context
            .map { AssistStructureUtil.traverseStructure(it.structure, packageManager) }
            .reduce { acc, structure -> acc.copy(items = acc.items + structure.items) }

        var login: String? = null
        var password: String? = null
        parsedStructure.items.forEach {
            if (it.type == ItemDataType.LOGIN) login = it.value else password = it.value
        }
        if (password != null && parsedStructure.appName != null) {
            val encryptedPass = cryptoManager.encryptData(password!!)
            val item = Item(
                id = 0,
                title = parsedStructure.appName,
                login = login,
                password = encryptedPass.ciphertext,
                initializationVector = encryptedPass.initializationVector,
                packageCertificate = parsedStructure.certificatesHashes,
            )
            runBlocking {
                itemRepository.updateOrCreateItem(item)
            }
        }
        saveCallback.onSuccess()
    }

    private fun buildRequiresAuthResponse(parsedStructure: AutofillParsedStructure): FillResponse.Builder {

        val intentSender = buildIntentSender()
        val dataSet = AutofillHelper.buildDataSet(
            autofillItems = parsedStructure.items,
            intentSender = intentSender,
        )

        return FillResponse.Builder()
            .addDataset(dataSet)
    }

    private fun buildAutofillResponse(parsedStructure: AutofillParsedStructure): FillResponse.Builder {
        val items = runBlocking {
            itemRepository.searchItemByCertificates(parsedStructure.certificatesHashes)
        }
        val responseBuilder = FillResponse.Builder()
        if (items.isNotEmpty()) {
            items.forEach { item ->
                val pass = cryptoManager.decryptData(item.password, item.initializationVector)
                responseBuilder.addDataset(
                    AutofillHelper.buildDataSet(
                        autofillItems = parsedStructure.items,
                        item = item,
                        password = pass,
                    )
                )
            }
        } else {
            responseBuilder.addDataset(
                AutofillHelper.buildDataSet(
                    autofillItems = parsedStructure.items,
                    remoteViews = AutofillHelper.buildNoItemFoundPresentation(this),
                    intentSender = buildIntentSender()
                )
            )
        }
        return responseBuilder
    }

    private fun buildSuggestCredentialsResponse(
        parsedStructure: AutofillParsedStructure,
        clientState: Bundle,
    ): FillResponse.Builder {
        val responseBuilder = FillResponse.Builder()
        val dataSets = AutofillHelper.buildSuggestedCredentialsDataSets(
            context = this,
            autofillItems = parsedStructure.items,
            intentSender = buildIntentSender(redirectToItem = true),
        )
        dataSets.forEach(responseBuilder::addDataset)
        val newClientState = AutofillHelper.buildClientState(clientState, parsedStructure.items)
        responseBuilder.setClientState(newClientState)
        responseBuilder.setSaveInfo(AutofillHelper.buildSaveInfo(newClientState))
        return responseBuilder
    }

    private fun buildIntentSender(redirectToItem: Boolean = false): IntentSender {
        val authIntent = Intent(this, AutofillActivity::class.java)
            .putExtra(AutofillActivity.REDIRECT_TO_ITEM_KEY, redirectToItem)

        return PendingIntent.getActivity(
            this,
            0,
            authIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        ).intentSender
    }
}