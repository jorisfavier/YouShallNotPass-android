package fr.jorisfavier.youshallnotpass.ui.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.utils.inflate

class SearchResultAdapter(
    private val onItemEditClicked: (Item) -> Unit,
    private val onDeleteItemClicked: (Item) -> Unit,
    private val decryptPassword: (Item) -> String,
    private val copyPasswordToClipboard: (Item) -> Unit
) : RecyclerView.Adapter<SearchResultViewHolder>() {

    private var results: List<Item> = ArrayList()
    private var lastExpandedViewHolder: SearchResultViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = parent.inflate(R.layout.viewholder_search_result, false)
        return SearchResultViewHolder(view)
    }

    override fun getItemCount(): Int {
        return results.size
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(
            results[position],
            onItemEditClicked,
            onDeleteItemClicked,
            decryptPassword,
            copyPasswordToClipboard
        )
        holder.itemView.setOnClickListener {
            if (lastExpandedViewHolder != holder) {
                lastExpandedViewHolder?.toggleViewState(false)
            }
            lastExpandedViewHolder = holder
            holder.toggleViewState(!holder.isExpanded)
        }
    }

    fun updateResults(newList: List<Item>) {
        results = newList
        notifyDataSetChanged()
    }
}