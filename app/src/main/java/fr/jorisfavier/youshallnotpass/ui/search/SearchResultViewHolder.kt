package fr.jorisfavier.youshallnotpass.ui.search

import android.animation.ValueAnimator
import android.text.InputType
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.utils.fadeIn
import fr.jorisfavier.youshallnotpass.utils.fadeOut
import fr.jorisfavier.youshallnotpass.utils.px

class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var view = itemView
        private set

    var isExpanded = false
        private set

    private var isPasswordVisible = false
    private var hasLoginField = false

    private val detailHeight
        get() = 150.px + if (hasLoginField) 70.px else 0
    private val detailPadding = (-10).px
    private val animDuration = 500L

    private val searchResultItemTitle: TextView by lazy { itemView.findViewById(R.id.searchResultItemTitle) }
    private val searchResultItemLoginLabel: TextView by lazy { itemView.findViewById(R.id.searchResultItemLoginTitle) }
    private val searchResultItemLogin: TextView by lazy { itemView.findViewById(R.id.searchResultItemLogin) }
    private val searchResultItemPassword: TextView by lazy { itemView.findViewById(R.id.searchResultItemPassword) }
    private val searchResultShowHideButton: Button by lazy { itemView.findViewById(R.id.searchResultShowHideButton) }
    private val searchResultCopyPasswordButton: Button by lazy { itemView.findViewById(R.id.searchResultCopyPasswordButton) }
    private val searchResultCopyLoginButton: Button by lazy { itemView.findViewById(R.id.searchResultCopyLoginButton) }
    private val searchResultItemEditButton: Button by lazy { itemView.findViewById(R.id.searchResultItemEditButton) }
    private val searchResultItemDeleteButton: Button by lazy { itemView.findViewById(R.id.searchResultItemDeleteButton) }
    private val searchResultItemCard: View by lazy { itemView.findViewById(R.id.searchResultItemCard) }
    private val searchResultMainContainer: View by lazy { itemView.findViewById(R.id.searchResultMainContainer) }
    private val searchResultItemDetail: View by lazy { itemView.findViewById(R.id.searchResultItemDetail) }


    fun bind(
        result: Item, onEditItemClicked: (Item) -> Unit,
        onDeleteItemClicked: (Item) -> Unit,
        decryptPassword: (Item) -> String,
        copyToClipboard: (Item, ItemDataType) -> Unit
    ) {
        searchResultItemTitle.text = result.title
        searchResultItemLogin.text = result.login
        hasLoginField = !result.login.isNullOrEmpty()
        searchResultCopyLoginButton.isVisible = !result.login.isNullOrEmpty()
        searchResultItemLogin.isVisible = !result.login.isNullOrEmpty()
        searchResultItemLoginLabel.isVisible = !result.login.isNullOrEmpty()
        searchResultShowHideButton.setOnClickListener { togglePasswordVisibility(decryptPassword(result)) }
        searchResultCopyPasswordButton.setOnClickListener { copyToClipboard(result, ItemDataType.PASSWORD) }
        searchResultCopyLoginButton.setOnClickListener { copyToClipboard(result, ItemDataType.LOGIN) }
        searchResultItemEditButton.setOnClickListener { onEditItemClicked.invoke(result) }
        searchResultItemDeleteButton.setOnClickListener { onDeleteItemClicked.invoke(result) }
    }

    fun toggleViewState(expand: Boolean) {
        val currentHeight = searchResultItemCard.measuredHeight
        val currentSidePadding = searchResultMainContainer.paddingStart
        val coeff = if (expand) 1f else -1f
        if (isExpanded != expand) {
            isExpanded = expand
            val anim = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = animDuration
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    (it.animatedValue as Float).let { progress ->
                        searchResultItemCard.layoutParams.height =
                            currentHeight + (detailHeight.toFloat() * progress * coeff).toInt()
                        val padding = currentSidePadding + (detailPadding.toFloat() * progress * coeff).toInt()
                        searchResultMainContainer.updatePadding(
                            left = padding,
                            right = padding
                        )
                        searchResultItemCard.requestLayout()
                    }
                }
                doOnStart {
                    if (isExpanded) {
                        searchResultItemDetail.fadeIn(animDuration)
                    } else {
                        searchResultItemDetail.fadeOut(animDuration)
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
            searchResultItemPassword.inputType =
                InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            searchResultItemPassword.inputType = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        searchResultItemPassword.text = text
    }

}