package fr.jorisfavier.youshallnotpass.features.search

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.viewholder_search_result.view.*

class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var view = itemView

    fun bind(result: String){
        view.viewholder_searchresult_text.text = result
    }
}