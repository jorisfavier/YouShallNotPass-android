package fr.jorisfavier.youshallnotpass.ui.settings.importitem.review

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.utils.extensions.inflate

class ImportedItemHeaderAdapter(
    private val onSelectAll: () -> Unit,
) : RecyclerView.Adapter<ImportedItemHeaderViewHolder>() {

    private var itemFoundCount: Int = 0

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ImportedItemHeaderViewHolder {
        val view = parent.inflate(R.layout.viewholder_import_review_header)
        return ImportedItemHeaderViewHolder(onSelectAll = onSelectAll, view)
    }

    override fun onBindViewHolder(holder: ImportedItemHeaderViewHolder, position: Int) {
        holder.bind(itemFoundCount)
    }

    fun onItemCountChanged(itemCount: Int) {
        this.itemFoundCount = itemCount
        notifyItemChanged(1)
    }

}
