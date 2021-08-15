package fr.jorisfavier.youshallnotpass.ui.autofill

import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.databinding.ViewholderAutofillResultBinding
import fr.jorisfavier.youshallnotpass.model.Item

class AutofillResultViewHolder(
    private val binding: ViewholderAutofillResultBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(result: Item, onItemClicked: (Item) -> Unit) {
        binding.itemName.text = result.title
        binding.root.setOnClickListener {
            onItemClicked(result)
        }
    }

}