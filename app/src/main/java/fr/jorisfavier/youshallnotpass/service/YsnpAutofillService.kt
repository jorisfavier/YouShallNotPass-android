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
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.manager.AuthManager
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.AutofillParsedStructure
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.repository.ItemRepository
import fr.jorisfavier.youshallnotpass.ui.autofill.AutofillActivity
import fr.jorisfavier.youshallnotpass.utils.AssistStructureUtil
import fr.jorisfavier.youshallnotpass.utils.autofill.AutofillHelperCompat
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class YsnpAutofillService : AutofillService() {

    @Inject
    lateinit var itemRepository: ItemRepository

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var cryptoManager: CryptoManager

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback,
    ) {
        val context: List<FillContext> = request.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure
        val parsedStructure = AssistStructureUtil.traverseStructure(structure, packageManager)
        val fillResponse =
            when {
                parsedStructure.isNewCredentials -> {
                    buildSuggestCredentialsResponse(
                        fillRequest = request,
                        parsedStructure = parsedStructure,
                        clientState = request.clientState ?: bundleOf(),
                    )
                }
                parsedStructure.items.isEmpty() -> {
                    null
                }
                parsedStructure.webDomain != null || !authManager.isAutofillUnlocked -> {
                    buildRequiresAuthResponse(
                        fillRequest = request,
                        parsedStructure = parsedStructure,
                    )
                }
                else -> {
                    buildAutofillResponse(
                        fillRequest = request,
                        parsedStructure = parsedStructure,
                    )
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

    private fun buildRequiresAuthResponse(
        fillRequest: FillRequest,
        parsedStructure: AutofillParsedStructure,
    ): FillResponse.Builder {

        val intentSender = buildIntentSender(fillRequest)
        val dataSet = AutofillHelperCompat.buildRequireAuthDataSet(
            context = this,
            fillRequest = fillRequest,
            autofillItems = parsedStructure.items,
            intentSender = intentSender,
        )

        return FillResponse.Builder()
            .addDataset(dataSet)
    }

    private fun buildAutofillResponse(
        fillRequest: FillRequest,
        parsedStructure: AutofillParsedStructure,
    ): FillResponse.Builder {
        val items = runBlocking {
            itemRepository.searchItemByCertificates(parsedStructure.certificatesHashes)
        }
        val responseBuilder = FillResponse.Builder()
        if (items.isNotEmpty()) {
            items.forEach { item ->
                val pass = cryptoManager.decryptData(item.password, item.initializationVector)
                responseBuilder.addDataset(
                    AutofillHelperCompat.buildItemDataSet(
                        context = this,
                        fillRequest = fillRequest,
                        autofillItems = parsedStructure.items,
                        item = item,
                        password = pass,
                    )
                )
            }
        } else {
            responseBuilder.addDataset(
                AutofillHelperCompat.buildNoItemFoundDataSet(
                    fillRequest = fillRequest,
                    context = this,
                    autofillItems = parsedStructure.items,
                    intentSender = buildIntentSender(fillRequest)
                )
            )
        }
        return responseBuilder
    }

    private fun buildSuggestCredentialsResponse(
        fillRequest: FillRequest,
        parsedStructure: AutofillParsedStructure,
        clientState: Bundle,
    ): FillResponse.Builder {
        val responseBuilder = FillResponse.Builder()
        val dataSets = AutofillHelperCompat.buildSuggestedCredentialsDataSets(
            fillRequest = fillRequest,
            context = this,
            autofillItems = parsedStructure.items,
            intentSender = buildIntentSender(redirectToItem = true, fillRequest = fillRequest),
        )
        dataSets.forEach(responseBuilder::addDataset)
        val newClientState =
            AutofillHelperCompat.buildClientState(clientState, parsedStructure.items)
        responseBuilder.setClientState(newClientState)
        responseBuilder.setSaveInfo(AutofillHelperCompat.buildSaveInfo(newClientState))
        return responseBuilder
    }

    private fun buildIntentSender(
        fillRequest: FillRequest,
        redirectToItem: Boolean = false,
    ): IntentSender {
        val authIntent = Intent(this, AutofillActivity::class.java)
            .putExtra(AutofillActivity.REDIRECT_TO_ITEM_KEY, redirectToItem)
            .putExtra(AutofillActivity.FILL_REQUEST_KEY, fillRequest)

        return PendingIntent.getActivity(
            this,
            0,
            authIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        ).intentSender
    }
}