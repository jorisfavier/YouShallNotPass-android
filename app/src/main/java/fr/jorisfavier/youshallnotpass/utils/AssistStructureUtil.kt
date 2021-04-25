package fr.jorisfavier.youshallnotpass.utils

import android.app.assist.AssistStructure
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import fr.jorisfavier.youshallnotpass.model.AutofillItemType
import fr.jorisfavier.youshallnotpass.model.AutofillParsedStructure

@RequiresApi(Build.VERSION_CODES.O)
object AssistStructureUtil {

    fun traverseStructure(structure: AssistStructure): List<AutofillParsedStructure> {
        val result = mutableListOf<AutofillParsedStructure>()
        val windowNodes: List<AssistStructure.WindowNode> =
            structure.run {
                (0 until windowNodeCount).map { getWindowNodeAt(it) }
            }

        windowNodes.forEach { windowNode: AssistStructure.WindowNode ->
            val viewNode: AssistStructure.ViewNode? = windowNode.rootViewNode
            result += traverseNode(viewNode)
        }
        return result
    }

    private fun traverseNode(viewNode: AssistStructure.ViewNode?): List<AutofillParsedStructure> {
        val result = mutableListOf<AutofillParsedStructure>()
        val parsedStructure = createParsedStructure(viewNode)
        if (parsedStructure != null) {
            result.add(parsedStructure)
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

    private fun createParsedStructure(viewNode: AssistStructure.ViewNode?): AutofillParsedStructure? {
        return when {
            viewNode?.autofillHints?.any {
                it == View.AUTOFILL_HINT_USERNAME || it == View.AUTOFILL_HINT_NAME || it == View.AUTOFILL_HINT_EMAIL_ADDRESS
            } == true -> AutofillParsedStructure(viewNode.autofillId!!, AutofillItemType.LOGIN)
            viewNode?.autofillHints?.any { it == View.AUTOFILL_HINT_PASSWORD } == true ->
                AutofillParsedStructure(viewNode.autofillId!!, AutofillItemType.PASSWORD)
            else -> null
        }
    }
}