package fr.jorisfavier.youshallnotpass.utils.autofill

import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.service.autofill.Dataset
import android.service.autofill.SaveInfo
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import fr.jorisfavier.youshallnotpass.BuildConfig
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.model.AutofillItem
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.utils.PasswordOptions
import fr.jorisfavier.youshallnotpass.utils.PasswordUtil


@RequiresApi(Build.VERSION_CODES.O)
object AutofillHelper26 {

    private const val PASSWORD_KEY = "password_key"
    private const val LOGIN_KEY = "login_key"

    fun buildItemDataSet(
        autofillItems: List<AutofillItem>,
        item: Item,
        password: String? = null,
    ): Dataset {
        val dataSet = Dataset.Builder()
        val remoteViews = buildItemPresentation(item)
        autofillItems.forEach {
            when (it.type) {
                ItemDataType.LOGIN -> {
                    dataSet.setValue(
                        it.id,
                        AutofillValue.forText(item.login),
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
        return dataSet.build()
    }

    fun buildRequireAuthDataSet(
        autofillItems: List<AutofillItem>,
        intentSender: IntentSender,
    ): Dataset {
        val dataSet = Dataset.Builder()
        val remoteViews = buildRequireAuthPresentation()
        autofillItems.forEach {
            dataSet.setValue(it.id, null, remoteViews)
        }
        dataSet.setAuthentication(intentSender)
        return dataSet.build()
    }

    fun buildNoItemFoundDataSet(
        context: Context,
        autofillItems: List<AutofillItem>,
        intentSender: IntentSender,
    ): Dataset {
        val dataSet = Dataset.Builder()
        val remoteViews = buildNoItemFoundPresentation(context)
        autofillItems.forEach {
            when (it.type) {
                ItemDataType.LOGIN -> {
                    dataSet.setValue(
                        it.id,
                        null,
                        remoteViews
                    )
                }
                ItemDataType.PASSWORD -> {
                    dataSet.setValue(
                        it.id,
                        null,
                        remoteViews
                    )
                }
            }
        }
        dataSet.setAuthentication(intentSender)
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
        if (autofillItems.any { it.type == ItemDataType.LOGIN }) {
            result.add(dataSetCreateItem.build())
        }
        if (autofillItems.any { it.type == ItemDataType.PASSWORD }) {
            result.add(dataSetUsePass.build())
        }
        return result
    }

    fun buildSaveInfo(clientState: Bundle): SaveInfo {
        val loginId = clientState.getParcelable(LOGIN_KEY) as? AutofillId
        val passwordId = clientState.getParcelable(PASSWORD_KEY) as? AutofillId
        val saveInfo = SaveInfo.Builder(
            SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD,
            listOfNotNull(loginId, passwordId).toTypedArray(),
        )
        if (loginId != null && passwordId != null) {
            saveInfo.setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE)
        }
        return saveInfo.build()
    }

    fun buildClientState(currentClientState: Bundle, autofillItems: List<AutofillItem>): Bundle {
        autofillItems.forEach {
            if (it.type == ItemDataType.LOGIN)
                currentClientState.putParcelable(LOGIN_KEY, it.id)
            if (it.type == ItemDataType.PASSWORD)
                currentClientState.putParcelable(PASSWORD_KEY, it.id)
        }
        return currentClientState
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

    fun buildRequireAuthPresentation(): RemoteViews {
        return RemoteViews(
            BuildConfig.APPLICATION_ID,
            R.layout.autofill_requires_authentication,
        )
    }

    fun buildItemPresentation(item: Item): RemoteViews {
        return RemoteViews(
            BuildConfig.APPLICATION_ID,
            R.layout.autofill_response
        ).apply {
            setTextViewText(R.id.title, item.title)
            setTextViewText(R.id.subtitle, item.login)
        }
    }
}