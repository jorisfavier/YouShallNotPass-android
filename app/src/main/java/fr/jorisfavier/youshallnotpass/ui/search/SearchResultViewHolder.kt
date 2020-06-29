package fr.jorisfavier.youshallnotpass.ui.search

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnStart
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.data.model.Item
import fr.jorisfavier.youshallnotpass.utils.fadeIn
import fr.jorisfavier.youshallnotpass.utils.fadeOut
import fr.jorisfavier.youshallnotpass.utils.px
import kotlinx.android.synthetic.main.viewholder_search_result.view.*

class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var view = itemView
        private set

    var isExpanded = false
        private set

    private val detailHeight = 150.px
    private val detailPadding = (-10).px
    private val animDuration = 500L


    fun bind(result: Item, onEditItemClicked: (Item) -> Unit) {
        view.searchResultItemTitle.text = result.title
        view.searchResultItemEditButton.setOnClickListener { onEditItemClicked.invoke(result) }
    }

    fun toggleViewState(expand: Boolean) {
        val currentHeight = view.searchResultItemCard.measuredHeight
        val currentSidePadding = view.searchResultMainContainer.paddingStart
        val coeff = if (expand) 1f else -1f
        if (isExpanded != expand) {
            isExpanded = expand
            val anim = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = animDuration
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    (it.animatedValue as Float).let { progress ->
                        view.searchResultItemCard.layoutParams.height =
                                currentHeight + (detailHeight.toFloat() * progress * coeff).toInt()
                        val padding = currentSidePadding + (detailPadding.toFloat() * progress * coeff).toInt()
                        view.searchResultMainContainer.updatePadding(
                                left = padding,
                                right = padding
                        )
                        view.searchResultItemCard.requestLayout()
                    }
                }
                doOnStart {
                    if (isExpanded) {
                        view.searchResultItemDetail.fadeIn(animDuration)
                    } else {
                        view.searchResultItemDetail.fadeOut(animDuration)
                    }
                }
            }
            anim.start()
        }
    }

}