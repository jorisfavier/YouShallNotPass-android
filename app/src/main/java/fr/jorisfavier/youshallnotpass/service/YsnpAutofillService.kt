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
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.ui.autofill.AutofillActivity
import fr.jorisfavier.youshallnotpass.utils.AssistStructureUtil
import fr.jorisfavier.youshallnotpass.utils.AutofillHelper
import timber.log.Timber
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class YsnpAutofillService : AutofillService() {

    @Inject
    lateinit var itemRepository: IItemRepository

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        // Get the structure from the request
        val context: List<FillContext> = request.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure
        // Traverse the structure looking for nodes to fill out.
        val parsedStructure = AssistStructureUtil.traverseStructure(structure, packageManager)

        if (parsedStructure.items.isNotEmpty()) {
            val authIntent = Intent(this, AutofillActivity::class.java)

            val intentSender: IntentSender = PendingIntent.getActivity(
                this,
                0,
                authIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            ).intentSender

            val dataSet = AutofillHelper.buildDataSet(
                autofillItems = parsedStructure.items,
                intentSender = intentSender,
            )

            val fillResponse: FillResponse = FillResponse.Builder()
                .addDataset(dataSet)
                .build()
            callback.onSuccess(fillResponse)
        } else {
            callback.onSuccess(null)
        }

    }

    override fun onSaveRequest(p0: SaveRequest, p1: SaveCallback) {
        Timber.d("onSaveReq")
    }
}