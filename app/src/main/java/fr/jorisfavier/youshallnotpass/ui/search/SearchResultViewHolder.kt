package fr.jorisfavier.youshallnotpass.ui.search

import android.animation.ValueAnimator
import android.text.InputType
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnStart
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.ViewholderSearchResultBinding
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.utils.fadeIn
import fr.jorisfavier.youshallnotpass.utils.fadeOut
import fr.jorisfavier.youshallnotpass.utils.px
import fr.jorisfavier.youshallnotpass.utils.toast
import timber.log.Timber

class SearchResultViewHolder(
    itemView: View,
    private val binding: ViewholderSearchResultBinding
) : RecyclerView.ViewHolder(itemView) {

    var view = itemView
        private set

    var isExpanded = false
        private set

    private var isPasswordVisible = false
    private var hasLoginField = false

    private val detailHeight
        get() = if (hasLoginField) DETAIL_HEIGHT_WITH_LOGIN else DETAIL_HEIGHT_WITHOUT_LOGIN

    fun bind(
        result: Item,
        onEditItemClicked: (Item) -> Unit,
        onDeleteItemClicked: (Item) -> Unit,
        decryptPassword: (Item) -> Result<String>,
        copyToClipboard: (Item, ItemDataType) -> Unit
    ) {
        toggleViewState(false, animate = false)
        isExpanded = false
        binding.item = result
        hasLoginField = result.hasLogin
        binding.searchResultItemShowHideButton.setOnClickListener {
            decryptPassword(result)
                .onSuccess { togglePasswordVisibility(it) }
                .onFailure {
                    Timber.e(it, "An error occurred while decrypting password")
                    view.context.toast(R.string.error_occurred)
                }

        }
        binding.searchResultItemCopyPasswordButton.setOnClickListener { copyToClipboard(result, ItemDataType.PASSWORD) }
        binding.searchResultItemCopyLoginButton.setOnClickListener { copyToClipboard(result, ItemDataType.LOGIN) }
        binding.searchResultItemEditButton.setOnClickListener { onEditItemClicked.invoke(result) }
        binding.searchResultItemDeleteButton.setOnClickListener { onDeleteItemClicked.invoke(result) }
        binding.executePendingBindings()
    }

    fun toggleViewState(expand: Boolean, animate: Boolean = true) {
        val currentHeight = binding.searchResultItemCard.measuredHeight
        val currentSidePadding = binding.searchResultMainContainer.paddingStart
        val coeff = if (expand) 1f else -1f
        val animationDuration = if (animate) ANIMATION_DURATION else 0L
        if (isExpanded != expand) {
            isExpanded = expand
            val anim = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = animationDuration
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    (it.animatedValue as Float).let { progress ->
                        binding.searchResultItemCard.layoutParams.height =
                            currentHeight + (detailHeight.toFloat() * progress * coeff).toInt()
                        val padding = currentSidePadding + (DETAIL_PADDING * progress * coeff).toInt()
                        binding.searchResultMainContainer.updatePadding(
                            left = padding,
                            right = padding
                        )
                        binding.searchResultItemCard.requestLayout()
                    }
                }
                doOnStart {
                    if (isExpanded) {
                        binding.searchResultItemDetail.fadeIn(animationDuration)
                    } else {
                        binding.searchResultItemDetail.fadeOut(animationDuration)
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
            binding.searchResultItemPassword.inputType =
                InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.searchResultItemPassword.inputType = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.searchResultItemPassword.text = text
    }

    companion object {
        private val DETAIL_HEIGHT_WITH_LOGIN = 220.px
        private val DETAIL_HEIGHT_WITHOUT_LOGIN = 150.px
        private val DETAIL_PADDING = (-10).px.toFloat()
        private const val ANIMATION_DURATION = 500L
    }

}