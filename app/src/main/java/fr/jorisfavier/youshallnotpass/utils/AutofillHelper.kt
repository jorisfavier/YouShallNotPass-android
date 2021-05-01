package fr.jorisfavier.youshallnotpass.utils

import android.content.IntentSender
import android.os.Build
import android.service.autofill.Dataset
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import fr.jorisfavier.youshallnotpass.BuildConfig
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.model.AutofillItem
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType


@RequiresApi(Build.VERSION_CODES.O)
object AutofillHelper {

    fun buildDataSet(
        autofillItems: List<AutofillItem>,
        item: Item? = null,
        password: String? = null,
        intentSender: IntentSender? = null,
    ): Dataset {
        val dataSet = Dataset.Builder()
        val remoteViews = buildPresentation(item)
        autofillItems.forEach {
            when (it.type) {
                ItemDataType.LOGIN -> {
                    dataSet.setValue(
                        it.id,
                        AutofillValue.forText(item?.login),
                        remoteViews
                    )
                }
                ItemDataType.PASSWORD -> {
                    dataSet.setValue(
                        it.id,
                        AutofillValue.forText(password),
                        remoteViews
                    )
                }
            }
        }
        if (intentSender != null) dataSet.setAuthentication(intentSender)
        return dataSet.build()
    }

    private fun buildPresentation(item: Item?): RemoteViews {
        return if (item != null) {
            RemoteViews(
                BuildConfig.APPLICATION_ID,
                R.layout.autofill_response
            ).apply {
                setTextViewText(R.id.title, item.title)
                setTextViewText(R.id.subtitle, item.login)
            }
        } else {
            RemoteViews(
                BuildConfig.APPLICATION_ID,
                R.layout.autofill_requires_authentication,
            )
        }
    }
}