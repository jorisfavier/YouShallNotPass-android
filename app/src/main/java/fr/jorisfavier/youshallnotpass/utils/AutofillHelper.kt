package fr.jorisfavier.youshallnotpass.utils

import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.SaveInfo
import android.view.View
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
        remoteViews: RemoteViews = buildPresentation(item),
    ): Dataset {
        val dataSet = Dataset.Builder()
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

    fun buildSuggestedCredentialsDataSets(
        context: Context,
        autofillItems: List<AutofillItem>,
        intentSender: IntentSender?,
    ): List<Dataset> {
        val result = mutableListOf<Dataset>()
        val dataSetUsePass = Dataset.Builder()
        val dataSetCreateItem = Dataset.Builder()
        autofillItems.forEach {
            when (it.type) {
                ItemDataType.LOGIN -> {
                    dataSetCreateItem.setValue(
                        it.id,
                        null,
                        buildCreateItemPresentation(context)
                    )
                }
                ItemDataType.PASSWORD -> {
                    val password = PasswordUtil.getSecurePassword(PasswordOptions.DEFAULT_OPTIONS)
                    dataSetUsePass.setValue(
                        it.id,
                        AutofillValue.forText(password),
                        buildCreatePasswordPresentation(context, password)
                    )
                    dataSetCreateItem.setValue(
                        it.id,
                        null,
                        buildCreateItemPresentation(context)
                    )
                }
            }
        }
        dataSetCreateItem.setAuthentication(intentSender)
        result.add(dataSetCreateItem.build())
        result.add(dataSetUsePass.build())
        return result
    }

    fun buildNoItemFoundPresentation(context: Context): RemoteViews {
        val noItemFound = context.getString(R.string.no_results_found)
        val searchItems = context.getString(R.string.search_for_item)
        return RemoteViews(
            BuildConfig.APPLICATION_ID,
            R.layout.autofill_response,
        ).apply {
            setTextViewText(R.id.title, noItemFound)
            setTextViewText(R.id.subtitle, searchItems)
        }
    }

    fun buildCreatePasswordPresentation(context: Context, password: String): RemoteViews {
        val generatedPassword = context.getString(R.string.use_generated_password)
        return RemoteViews(
            BuildConfig.APPLICATION_ID,
            R.layout.autofill_response,
        ).apply {
            setTextViewText(R.id.title, generatedPassword)
            setTextViewText(R.id.subtitle, password)
        }
    }

    fun buildCreateItemPresentation(context: Context): RemoteViews {
        val createItem = context.getString(R.string.create_new_item)
        return RemoteViews(
            BuildConfig.APPLICATION_ID,
            R.layout.autofill_response,
        ).apply {
            setTextViewText(R.id.title, createItem)
            setViewVisibility(R.id.subtitle, View.GONE)
        }
    }

    fun buildSaveInfo(autofillItems: List<AutofillItem>): SaveInfo {
        return SaveInfo.Builder(
            SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD,
            autofillItems.map { it.id }.toTypedArray(),
        ).setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE).build()
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