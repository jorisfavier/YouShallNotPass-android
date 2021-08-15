package fr.jorisfavier.youshallnotpass.utils.autofill

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.BlendMode
import android.graphics.drawable.Icon
import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.InlinePresentation
import android.view.autofill.AutofillValue
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.v1.InlineSuggestionUi
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.model.AutofillItem
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.utils.PasswordOptions
import fr.jorisfavier.youshallnotpass.utils.PasswordUtil
import fr.jorisfavier.youshallnotpass.utils.extensions.getThemeColor


@RequiresApi(Build.VERSION_CODES.R)
object AutofillHelper30 {

    fun buildRequireAuthDataSet(
        context: Context,
        inlineSpec: InlinePresentationSpec,
        autofillItems: List<AutofillItem>,
        intentSender: IntentSender,
    ): Dataset {
        val dataSet = Dataset.Builder()
        val remoteViews = AutofillHelper26.buildRequireAuthPresentation()
        val inlinePresentation = buildRequireAuthInlinePresentation(
            context = context,
            inlineSpec = inlineSpec,
        )
        autofillItems.forEach {
            dataSet.setValue(it.id, null, remoteViews, inlinePresentation)
        }
        dataSet.setAuthentication(intentSender)
        return dataSet.build()
    }

    fun buildSuggestedCredentialsDataSets(
        inlineSpec: InlinePresentationSpec,
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
                        AutofillHelper26.buildCreateItemPresentation(context),
                        buildInlineCreateItemPresentation(context, inlineSpec),
                    )
                }
                ItemDataType.PASSWORD -> {
                    val password = PasswordUtil.getSecurePassword(PasswordOptions.DEFAULT_OPTIONS)
                    dataSetUsePass.setValue(
                        it.id,
                        AutofillValue.forText(password),
                        AutofillHelper26.buildCreatePasswordPresentation(context, password),
                        buildInlineCreatePasswordPresentation(context, inlineSpec),
                    )
                    dataSetCreateItem.setValue(
                        it.id,
                        null,
                        AutofillHelper26.buildCreateItemPresentation(context),
                        buildInlineCreateItemPresentation(context, inlineSpec),
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

    fun buildItemDataSet(
        context: Context,
        inlineSpec: InlinePresentationSpec,
        autofillItems: List<AutofillItem>,
        item: Item,
        password: String? = null,
    ): Dataset {
        val dataSet = Dataset.Builder()
        val remoteViews = AutofillHelper26.buildItemPresentation(item)
        val inlineView = buildInlineItemPresentation(context, item, inlineSpec)
        autofillItems.forEach {
            when (it.type) {
                ItemDataType.LOGIN -> {
                    dataSet.setValue(
                        it.id,
                        AutofillValue.forText(item.login),
                        remoteViews,
                        inlineView,
                    )
                }
                ItemDataType.PASSWORD -> {
                    dataSet.setValue(
                        it.id,
                        AutofillValue.forText(password),
                        remoteViews,
                        inlineView,
                    )
                }
            }
        }
        return dataSet.build()
    }

    fun buildNoItemFoundDataSet(
        inlineSpec: InlinePresentationSpec,
        context: Context,
        autofillItems: List<AutofillItem>,
        intentSender: IntentSender,
    ): Dataset {
        val dataSet = Dataset.Builder()
        val remoteViews = AutofillHelper26.buildNoItemFoundPresentation(context)
        val inlineView = buildInlineNoItemFoundPresentation(
            context = context,
            inlinePresentationSpec = inlineSpec,
        )
        autofillItems.forEach {
            when (it.type) {
                ItemDataType.LOGIN -> {
                    dataSet.setValue(
                        it.id,
                        null,
                        remoteViews,
                        inlineView,
                    )
                }
                ItemDataType.PASSWORD -> {
                    dataSet.setValue(
                        it.id,
                        null,
                        remoteViews,
                        inlineView,
                    )
                }
            }
        }
        dataSet.setAuthentication(intentSender)
        return dataSet.build()
    }

    private fun buildRequireAuthInlinePresentation(
        context: Context,
        inlineSpec: InlinePresentationSpec,
    ): InlinePresentation {
        val slice = InlineSuggestionUi
            .newContentBuilder(PendingIntent.getActivity(context, 0, Intent(), 0))
            .setTitle(context.getString(R.string.authentication_required))
            .setStartIcon(Icon.createWithResource(context, R.drawable.ysnp_inline).apply {
                this.setTintBlendMode(BlendMode.DST)
                this.setTint(context.getThemeColor(R.attr.colorPrimary))
            })
            .build().slice
        return InlinePresentation(slice, inlineSpec, false)
    }

    private fun buildInlineCreateItemPresentation(
        context: Context,
        inlineSpec: InlinePresentationSpec,
    ): InlinePresentation {
        val createItem = context.getString(R.string.create_new_item)
        val slice = InlineSuggestionUi
            .newContentBuilder(PendingIntent.getActivity(context, 0, Intent(), 0))
            .setTitle(createItem)
            .setStartIcon(Icon.createWithResource(context, R.drawable.ysnp_inline).apply {
                this.setTintBlendMode(BlendMode.DST)
                this.setTint(context.getThemeColor(R.attr.colorPrimary))
            })
            .build().slice
        return InlinePresentation(slice, inlineSpec, false)
    }

    private fun buildInlineCreatePasswordPresentation(
        context: Context,
        inlineSpec: InlinePresentationSpec,
    ): InlinePresentation {
        val generatedPassword = context.getString(R.string.use_generated_password)
        val slice = InlineSuggestionUi
            .newContentBuilder(PendingIntent.getActivity(context, 0, Intent(), 0))
            .setTitle(generatedPassword)
            .setStartIcon(Icon.createWithResource(context, R.drawable.ysnp_inline).apply {
                this.setTintBlendMode(BlendMode.DST)
                this.setTint(context.getThemeColor(R.attr.colorPrimary))
            })
            .build().slice
        return InlinePresentation(slice, inlineSpec, false)
    }

    private fun buildInlineItemPresentation(
        context: Context,
        item: Item,
        inlinePresentationSpec: InlinePresentationSpec,
    ): InlinePresentation {
        val slice = InlineSuggestionUi
            .newContentBuilder(PendingIntent.getActivity(context, 0, Intent(), 0))
            .setTitle(item.title)
            .setSubtitle(item.login.orEmpty())
            .setStartIcon(Icon.createWithResource(context, R.drawable.ysnp_inline).apply {
                this.setTintBlendMode(BlendMode.DST)
                this.setTint(context.getThemeColor(R.attr.colorPrimary))
            })
            .build().slice
        return InlinePresentation(slice, inlinePresentationSpec, false)
    }

    private fun buildInlineNoItemFoundPresentation(
        context: Context,
        inlinePresentationSpec: InlinePresentationSpec,
    ): InlinePresentation {
        val noItemFound = context.getString(R.string.no_results_found)
        val slice = InlineSuggestionUi
            .newContentBuilder(PendingIntent.getActivity(context, 0, Intent(), 0))
            .setTitle(noItemFound)
            .setStartIcon(Icon.createWithResource(context, R.drawable.ysnp_inline).apply {
                this.setTintBlendMode(BlendMode.DST)
                this.setTint(context.getThemeColor(R.attr.colorPrimary))
            })
            .build().slice
        return InlinePresentation(slice, inlinePresentationSpec, false)
    }
}