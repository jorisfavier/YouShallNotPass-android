package fr.jorisfavier.youshallnotpass.ui.settings.importitem.review

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.utils.extensions.inflate

class ImportedItemAdapter(
    private val onItemClicked: (ExternalItem) -> Unit,
) : ListAdapter<SelectableExternalItem, ImportedItemViewHolder>(
    SelectableExternalItem.diffCallback
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImportedItemViewHolder {
        val view = parent.inflate(R.layout.viewholder_imported_item)
        return ImportedItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImportedItemViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item, onItemClicked)
    }

    override fun onBindViewHolder(
        holder: ImportedItemViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        val payload = payloads.firstOrNull() as? Boolean
        if (payload != null) {
            holder.bindSelection(payload)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}
