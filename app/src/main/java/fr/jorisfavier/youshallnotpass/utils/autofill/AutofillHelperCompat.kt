package fr.jorisfavier.youshallnotpass.utils.autofill

import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.service.autofill.Dataset
import android.service.autofill.FillRequest
import android.service.autofill.SaveInfo
import androidx.annotation.RequiresApi
import fr.jorisfavier.youshallnotpass.model.AutofillItem
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.utils.extensions.inlinePresentationSpec

@RequiresApi(Build.VERSION_CODES.O)
object AutofillHelperCompat {

    fun buildItemDataSet(
        context: Context,
        fillRequest: FillRequest,
        autofillItems: List<AutofillItem>,
        item: Item,
        password: String? = null,
    ): Dataset {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            && fillRequest.inlinePresentationSpec != null
        ) {
            AutofillHelper30.buildItemDataSet(
                context = context,
                inlineSpec = fillRequest.inlinePresentationSpec!!,
                autofillItems = autofillItems,
                item = item,
                password = password,
            )
        } else {
            AutofillHelper26.buildItemDataSet(
                autofillItems = autofillItems,
                item = item,
                password = password,
            )
        }
    }

    fun buildRequireAuthDataSet(
        fillRequest: FillRequest,
        context: Context,
        autofillItems: List<AutofillItem>,
        intentSender: IntentSender,
    ): Dataset {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            && fillRequest.inlinePresentationSpec != null
        ) {
            AutofillHelper30.buildRequireAuthDataSet(
                context = context,
                inlineSpec = fillRequest.inlinePresentationSpec!!,
                autofillItems = autofillItems,
                intentSender = intentSender
            )
        } else {
            AutofillHelper26.buildRequireAuthDataSet(
                autofillItems = autofillItems,
                intentSender = intentSender,
            )
        }
    }

    fun buildNoItemFoundDataSet(
        fillRequest: FillRequest,
        context: Context,
        autofillItems: List<AutofillItem>,
        intentSender: IntentSender,
    ): Dataset {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            && fillRequest.inlinePresentationSpec != null
        ) {
            AutofillHelper30.buildNoItemFoundDataSet(
                context = context,
                inlineSpec = fillRequest.inlinePresentationSpec!!,
                autofillItems = autofillItems,
                intentSender = intentSender
            )
        } else {
            AutofillHelper26.buildNoItemFoundDataSet(
                context = context,
                autofillItems = autofillItems,
                intentSender = intentSender,
            )
        }
    }


    fun buildSuggestedCredentialsDataSets(
        fillRequest: FillRequest,
        context: Context,
        autofillItems: List<AutofillItem>,
        intentSender: IntentSender?,
    ): List<Dataset> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            && fillRequest.inlinePresentationSpec != null
        ) {
            AutofillHelper30.buildSuggestedCredentialsDataSets(
                context = context,
                inlineSpec = fillRequest.inlinePresentationSpec!!,
                autofillItems = autofillItems,
                intentSender = intentSender
            )
        } else {
            AutofillHelper26.buildSuggestedCredentialsDataSets(
                context = context,
                autofillItems = autofillItems,
                intentSender = intentSender,
            )
        }
    }

    fun buildSaveInfo(clientState: Bundle): SaveInfo {
        return AutofillHelper26.buildSaveInfo(clientState)
    }

    fun buildClientState(currentClientState: Bundle, autofillItems: List<AutofillItem>): Bundle {
        return AutofillHelper26.buildClientState(
            currentClientState = currentClientState,
            autofillItems = autofillItems,
        )
    }
}