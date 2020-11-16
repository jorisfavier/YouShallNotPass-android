package fr.jorisfavier.youshallnotpass.ui.settings.importitem.review

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R

class ImportedItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(item: ExternalItemViewModel) {
        itemView.findViewById<TextView>(R.id.importedItemValues).text =
            "${item.externalItem.title} - ${item.externalItem.login} - ${item.externalItem.password}"
        itemView.findViewById<CheckBox>(R.id.importItemCheckbox).apply {
            isChecked = item.selected
            setOnCheckedChangeListener { _, checked -> item.selected = checked }
        }
    }
}