package fr.jorisfavier.youshallnotpass.ui.search

import android.animation.ValueAnimator
import android.text.InputType
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnStart
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.data.model.ItemEntity
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.utils.fadeIn
import fr.jorisfavier.youshallnotpass.utils.fadeOut
import fr.jorisfavier.youshallnotpass.utils.px
import kotlinx.android.synthetic.main.viewholder_search_result.view.*

class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var view = itemView
        private set

    var isExpanded = false
        private set

    private var isPasswordVisible = false

    private val detailHeight = 150.px
    private val detailPadding = (-10).px
    private val animDuration = 500L


    fun bind(result: Item, onEditItemClicked: (Item) -> Unit,
             onDeleteItemClicked: (Item) -> Unit,
             decryptPassword: (Item) -> String,
             copyToClipboard: (Item) -> Unit) {
        view.searchResultItemTitle.text = result.title
        view.searchResultShowHideButton.setOnClickListener { togglePasswordVisibility(decryptPassword(result)) }
        view.searchResultCopyButton.setOnClickListener { copyToClipboard(result) }
        view.searchResultItemEditButton.setOnClickListener { onEditItemClicked.invoke(result) }
        view.searchResultItemDeleteButton.setOnClickListener { onDeleteItemClicked.invoke(result) }
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

    private fun togglePasswordVisibility(password: String) {
        isPasswordVisible = !isPasswordVisible
        var text = view.context.getText(R.string.item_password)
        if (isPasswordVisible) {
            text = password
            view.searchResultItemPassword.inputType =
                    InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            view.searchResultItemPassword.inputType = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        view.searchResultItemPassword.text = text
    }

}