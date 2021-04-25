package fr.jorisfavier.youshallnotpass.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.databinding.ViewholderSearchResultBinding
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType

class SearchResultAdapter(
    private val onItemEditClicked: (Item) -> Unit,
    private val onDeleteItemClicked: (Item) -> Unit,
    private val decryptPassword: (Item) -> Result<String>,
    private val copyPasswordToClipboard: (Item, ItemDataType) -> Unit,
    private val sendToDesktop: (Item, ItemDataType) -> Unit
) : ListAdapter<Item, RecyclerView.ViewHolder>(Item.diffCallback) {

    private var lastExpandedViewHolder: SearchResultViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ViewholderSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as? SearchResultViewHolder ?: return
        holder.bind(
            getItem(position),
            onItemEditClicked,
            onDeleteItemClicked,
            decryptPassword,
            copyPasswordToClipboard,
            sendToDesktop
        )
        holder.itemView.setOnClickListener {
            if (lastExpandedViewHolder != holder) {
                lastExpandedViewHolder?.toggleViewState(false)
            }
            lastExpandedViewHolder = holder
            holder.toggleViewState(!holder.isExpanded)
        }
    }

    fun removeItem(item: Item) {
        val newList = currentList.toMutableList().apply {
            remove(item)
        }
        submitList(newList)
    }
}