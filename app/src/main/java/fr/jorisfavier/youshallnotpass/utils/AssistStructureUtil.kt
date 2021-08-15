package fr.jorisfavier.youshallnotpass.utils

import android.app.assist.AssistStructure
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.view.autofill.AutofillId
import androidx.annotation.RequiresApi
import androidx.autofill.HintConstants
import fr.jorisfavier.youshallnotpass.model.AutofillItem
import fr.jorisfavier.youshallnotpass.model.AutofillParsedStructure
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.utils.extensions.getAppName
import fr.jorisfavier.youshallnotpass.utils.extensions.getCertificateHashes

@RequiresApi(Build.VERSION_CODES.O)
object AssistStructureUtil {

    @JvmStatic
    fun traverseStructure(
        structure: AssistStructure,
        packageManager: PackageManager,
    ): AutofillParsedStructure {
        val viewNodes = mutableListOf<AssistStructure.ViewNode>()
        val autofillItems = mutableListOf<AutofillItem>()
        val ignoreIds = mutableListOf<AutofillId>()
        var newCredentials = false
        val packageName = structure.activityComponent.packageName
        var webDomain: String? = null
        val windowNodes: List<AssistStructure.WindowNode> =
            structure.run {
                (0 until windowNodeCount).map { getWindowNodeAt(it) }
            }

        windowNodes.forEach { windowNode ->
            val viewNode: AssistStructure.ViewNode? = windowNode.rootViewNode
            viewNodes += traverseNode(viewNode)
        }
        for (viewNode in viewNodes) {
            val autofillItem = createAutofillItem(viewNode)
            val autofillId = viewNode.autofillId
            if (autofillItem != null) {
                autofillItems.add(autofillItem)
                if (viewNode.webDomain != null) {
                    webDomain = viewNode.webDomain
                }
            } else if (autofillId != null) {
                ignoreIds.add(autofillId)
            }
            newCredentials = newCredentials || isCredentialsCreationNode(viewNode)
        }
        return AutofillParsedStructure(
            webDomain = webDomain,
            appName = packageManager.getAppName(packageName) ?: packageName,
            certificatesHashes = packageManager.getCertificateHashes(packageName),
            items = autofillItems,
            ignoreIds = ignoreIds,
            isNewCredentials = newCredentials,
        )
    }

    private fun traverseNode(viewNode: AssistStructure.ViewNode?): List<AssistStructure.ViewNode> {
        val result = mutableListOf<AssistStructure.ViewNode>()
        if (viewNode != null) {
            result.add(viewNode)
        }
        val children: List<AssistStructure.ViewNode>? =
            viewNode?.run {
                (0 until childCount).map { getChildAt(it) }
            }

        children?.forEach { childNode: AssistStructure.ViewNode ->
            result += traverseNode(childNode)
        }
        return result
    }

    private fun createAutofillItem(viewNode: AssistStructure.ViewNode): AutofillItem? {
        return when {
            viewNode.autofillHints?.any {
                it == View.AUTOFILL_HINT_USERNAME
                        || it == View.AUTOFILL_HINT_EMAIL_ADDRESS
                        || it == HintConstants.AUTOFILL_HINT_NEW_USERNAME
            } == true -> AutofillItem(
                id = viewNode.autofillId!!,
                value = viewNode.text?.toString().orEmpty(),
                type = ItemDataType.LOGIN,
            )
            viewNode.autofillHints?.any {
                it == View.AUTOFILL_HINT_PASSWORD
                        || it == HintConstants.AUTOFILL_HINT_NEW_PASSWORD
            } == true ->
                AutofillItem(
                    id = viewNode.autofillId!!,
                    value = viewNode.text?.toString().orEmpty(),
                    type = ItemDataType.PASSWORD,
                )
            else -> null
        }
    }

    private fun isCredentialsCreationNode(viewNode: AssistStructure.ViewNode): Boolean {
        return viewNode.autofillHints?.any {
            it == HintConstants.AUTOFILL_HINT_NEW_PASSWORD
                    || it == HintConstants.AUTOFILL_HINT_NEW_USERNAME
        } == true
    }
}