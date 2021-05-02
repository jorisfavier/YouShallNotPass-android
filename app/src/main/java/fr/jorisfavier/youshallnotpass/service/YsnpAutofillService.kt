package fr.jorisfavier.youshallnotpass.service

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import androidx.annotation.RequiresApi
import dagger.android.AndroidInjection
import fr.jorisfavier.youshallnotpass.manager.IAuthManager
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.model.AutofillParsedStructure
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.ui.autofill.AutofillActivity
import fr.jorisfavier.youshallnotpass.utils.AssistStructureUtil
import fr.jorisfavier.youshallnotpass.utils.AutofillHelper
import kotlinx.coroutines.runBlocking
import timber.log.Timber
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
        if (parsedStructure.items.isNotEmpty()) {
            val fillResponse =
                if (parsedStructure.webDomain != null || !authManager.isAutofillUnlocked) {
                    buildRequiresAuthResponse(parsedStructure)
                } else {
                    buildAutofillResponse(parsedStructure)
                }
            callback.onSuccess(fillResponse)
        } else {
            callback.onSuccess(null)
        }
    }

    override fun onSaveRequest(p0: SaveRequest, p1: SaveCallback) {
        Timber.d("onSaveReq")
    }

    private fun buildRequiresAuthResponse(parsedStructure: AutofillParsedStructure): FillResponse {

        val intentSender = buildIntentSender()
        val dataSet = AutofillHelper.buildDataSet(
            autofillItems = parsedStructure.items,
            intentSender = intentSender,
        )

        return FillResponse.Builder()
            .addDataset(dataSet)
            .build()
    }

    private fun buildAutofillResponse(parsedStructure: AutofillParsedStructure): FillResponse {
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
        return responseBuilder.build()
    }

    private fun buildIntentSender(): IntentSender? {
        val authIntent = Intent(this, AutofillActivity::class.java)

        return PendingIntent.getActivity(
            this,
            0,
            authIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        ).intentSender
    }
}