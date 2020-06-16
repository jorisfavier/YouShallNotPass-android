package fr.jorisfavier.youshallnotpass.ui.search

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.data.model.Item
import kotlinx.android.synthetic.main.viewholder_search_result.view.*

class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var view = itemView

    fun bind(result: Item){
        view.viewholder_searchresult_text.text = result.title
    }
}