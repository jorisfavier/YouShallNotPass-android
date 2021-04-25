package fr.jorisfavier.youshallnotpass.ui.autofill

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.databinding.ViewholderAutofillResultBinding
import fr.jorisfavier.youshallnotpass.model.Item

class AutofillAdapter(
    private val onItemClicked: (Item) -> Unit
) : ListAdapter<Item, RecyclerView.ViewHolder>(Item.diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AutofillResultViewHolder(
            ViewholderAutofillResultBinding.inflate(
                inflater,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as? AutofillResultViewHolder ?: return
        holder.bind(getItem(position), onItemClicked)
    }

}