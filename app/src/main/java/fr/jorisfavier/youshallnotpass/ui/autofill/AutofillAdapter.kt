package fr.jorisfavier.youshallnotpass.ui.autofill

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import fr.jorisfavier.youshallnotpass.databinding.ViewholderAutofillResultBinding
import fr.jorisfavier.youshallnotpass.model.Item

class AutofillAdapter(
    private val onItemClicked: (Item) -> Unit
) : ListAdapter<Item, AutofillResultViewHolder>(Item.diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutofillResultViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AutofillResultViewHolder(
            ViewholderAutofillResultBinding.inflate(
                inflater,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AutofillResultViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClicked)
    }

}