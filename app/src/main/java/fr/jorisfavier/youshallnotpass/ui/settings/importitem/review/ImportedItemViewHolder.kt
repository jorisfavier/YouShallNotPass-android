package fr.jorisfavier.youshallnotpass.ui.settings.importitem.review

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R

class ImportedItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: ExternalItemViewModel) {
        itemView.findViewById<TextView>(R.id.imported_item_values).text =
            "${item.externalItem.title} - ${item.externalItem.login} - ${item.externalItem.password}"
        itemView.findViewById<CheckBox>(R.id.imported_item_checkbox).apply {
            isChecked = item.selected
            setOnClickListener { item.selected = isChecked }
        }
    }
}