package fr.jorisfavier.youshallnotpass.service

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import dagger.android.AndroidInjection
import fr.jorisfavier.youshallnotpass.BuildConfig
import fr.jorisfavier.youshallnotpass.model.AutofillItemType
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.ui.autofill.AutofillActivity
import fr.jorisfavier.youshallnotpass.utils.AssistStructureUtil
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
        val results = AssistStructureUtil.traverseStructure(structure)
        Timber.d("zbri - $results")

        if (results.isNotEmpty()) {
            val authPresentation =
                RemoteViews(BuildConfig.APPLICATION_ID, android.R.layout.simple_list_item_1).apply {
                    setTextViewText(android.R.id.text1, "requires authentication")
                }
            val authIntent = Intent(this, AutofillActivity::class.java)

            val intentSender: IntentSender = PendingIntent.getActivity(
                this,
                0,
                authIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            ).intentSender

            val dataSet = Dataset.Builder()
            results.forEach {
                when (it.type) {
                    AutofillItemType.LOGIN -> {
                        dataSet.setValue(
                            it.id,
                            null,
                            authPresentation,
                        )
                    }
                    AutofillItemType.PASSWORD -> {
                        dataSet.setValue(
                            it.id,
                            null,
                            authPresentation,
                        )
                    }
                }
            }
            dataSet.setAuthentication(intentSender)

            // Build a FillResponse object that requires authentication.
            val fillResponse: FillResponse = FillResponse.Builder()
                .addDataset(dataSet.build())
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