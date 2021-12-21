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
    private val sendToDesktop: (Item, ItemDataType) -> Unit,
) : ListAdapter<Item, RecyclerView.ViewHolder>(Item.diffCallback) {

    sealed class Payload {
        data class Selection(val selectedId: Int) : Payload()
    }

    private var lastSelectedId: Int? = null

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
        val item = getItem(position)
        holder.bind(
            result = getItem(position),
            isSelected = lastSelectedId == item.id,
            onEditItemClicked = onItemEditClicked,
            onDeleteItemClicked = onDeleteItemClicked,
            decryptPassword = decryptPassword,
            copyToClipboard = copyPasswordToClipboard,
            sendToDesktop = sendToDesktop,
        )
        holder.itemView.setOnClickListener {
            val itemId = if (item.id == lastSelectedId) 0 else item.id
            lastSelectedId = itemId
            notifyItemRangeChanged(0, itemCount, Payload.Selection(itemId))
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        val holder = holder as? SearchResultViewHolder ?: return
        val item = getItem(position)
        val payload = payloads.firstOrNull() as? Payload
        if (payload != null) {
            when (payload) {
                is Payload.Selection -> holder.bindSelection(item.id == payload.selectedId)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}