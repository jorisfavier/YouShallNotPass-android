package fr.jorisfavier.youshallnotpass.ui.settings.importitem.review

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.utils.extensions.inflate

class ImportedItemAdapter(private var items: List<ExternalItemViewModel>) : RecyclerView.Adapter<ImportedItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImportedItemViewHolder {
        val view = parent.inflate(R.layout.viewholder_imported_item)
        return ImportedItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImportedItemViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    fun updateData(items: List<ExternalItemViewModel>) {
        this.items = items
        notifyDataSetChanged()
    }
}