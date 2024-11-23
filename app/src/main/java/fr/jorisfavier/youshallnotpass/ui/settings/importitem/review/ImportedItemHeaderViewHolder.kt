package fr.jorisfavier.youshallnotpass.ui.settings.importitem.review

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R

class ImportedItemHeaderViewHolder(
    onSelectAll: () -> Unit,
    view: View,
) : RecyclerView.ViewHolder(view) {

    init {
        view.findViewById<Button>(R.id.import_review_select_all).setOnClickListener {
            onSelectAll()
        }
    }

    fun bind(itemCount: Int) {
        if (itemCount <= 0) return
        itemView.findViewById<TextView>(R.id.import_review_item_count).text =
            itemView.context.getString(R.string.item_found, itemCount)
    }
}
