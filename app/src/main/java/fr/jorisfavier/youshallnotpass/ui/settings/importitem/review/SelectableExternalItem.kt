package fr.jorisfavier.youshallnotpass.ui.settings.importitem.review

import androidx.recyclerview.widget.DiffUtil
import fr.jorisfavier.youshallnotpass.model.ExternalItem

data class SelectableExternalItem(
    val externalItem: ExternalItem,
    val isSelected: Boolean,
) {
    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<SelectableExternalItem>() {
            override fun areItemsTheSame(
                oldItem: SelectableExternalItem,
                newItem: SelectableExternalItem,
            ): Boolean = oldItem.externalItem.title == newItem.externalItem.title

            override fun areContentsTheSame(
                oldItem: SelectableExternalItem,
                newItem: SelectableExternalItem,
            ): Boolean = oldItem == newItem

            override fun getChangePayload(
                oldItem: SelectableExternalItem,
                newItem: SelectableExternalItem,
            ): Any? {
                return if (oldItem.isSelected != newItem.isSelected) {
                    newItem.isSelected
                } else {
                    null
                }
            }
        }
    }
}
