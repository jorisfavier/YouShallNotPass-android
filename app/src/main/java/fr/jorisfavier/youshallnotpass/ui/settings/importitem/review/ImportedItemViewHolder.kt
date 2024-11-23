package fr.jorisfavier.youshallnotpass.ui.settings.importitem.review

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.model.ExternalItem

class ImportedItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        item: SelectableExternalItem,
        onClick: (ExternalItem) -> Unit,
    ) {
        itemView.findViewById<TextView>(R.id.imported_item_values).text =
            "${item.externalItem.title} - ${item.externalItem.login} - ${item.externalItem.password}"
        itemView.findViewById<CheckBox>(R.id.imported_item_checkbox).apply {
            isChecked = item.isSelected
            setOnClickListener { onClick(item.externalItem) }
        }
    }

    fun bindSelection(isSelected: Boolean) {
        itemView.findViewById<CheckBox>(R.id.imported_item_checkbox).isChecked = isSelected
    }
}
