package fr.jorisfavier.youshallnotpass.ui.search

import android.animation.ValueAnimator
import android.text.method.PasswordTransformationMethod
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.ViewholderSearchResultBinding
import fr.jorisfavier.youshallnotpass.model.Item
import fr.jorisfavier.youshallnotpass.model.ItemDataType
import fr.jorisfavier.youshallnotpass.utils.extensions.fadeIn
import fr.jorisfavier.youshallnotpass.utils.extensions.fadeOut
import fr.jorisfavier.youshallnotpass.utils.extensions.px
import fr.jorisfavier.youshallnotpass.utils.extensions.toast
import kotlinx.coroutines.launch

class SearchResultViewHolder(
    private val binding: ViewholderSearchResultBinding,
    private val onAnimationEnd: () -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    var view = itemView
        private set

    private var isExpanded = false
    private var isPasswordVisible = false
    private var hasLoginField = false

    private val detailHeight
        get() = if (hasLoginField) DETAIL_HEIGHT_WITH_LOGIN else DETAIL_HEIGHT_WITHOUT_LOGIN

    private var isAnimating = false

    fun bind(
        result: Item,
        isSelected: Boolean,
        onEditItemClicked: (Item) -> Unit,
        onDeleteItemClicked: (Item) -> Unit,
        decryptPassword: suspend (Item) -> Result<String>,
        copyToClipboard: (Item, ItemDataType) -> Unit,
        sendToDesktop: (Item, ItemDataType) -> Unit,
    ) {
        with(binding) {
            searchResultItemTitle.text = result.title
            searchResultItemLoginTitle.isVisible = result.hasLogin
            searchResultItemLogin.text = result.login
            searchResultItemLogin.isVisible = result.hasLogin
            searchResultItemCopyLoginButton.isVisible = result.hasLogin
            searchResultItemLoginDesktopButton.isVisible = result.hasLogin
            toggleViewState(isSelected, animate = false)
            hasLoginField = result.hasLogin
            searchResultItemPassword.transformationMethod = PasswordTransformationMethod()
            searchResultItemShowHideButton.setOnClickListener {
                view.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                    decryptPassword(result)
                        .onSuccess { togglePasswordVisibility(it) }
                        .onFailure {
                            view.context.toast(R.string.error_occurred)
                        }
                }

            }
            searchResultItemCopyPasswordButton.setOnClickListener {
                copyToClipboard(
                    result,
                    ItemDataType.PASSWORD
                )
            }
            searchResultItemCopyLoginButton.setOnClickListener {
                copyToClipboard(
                    result,
                    ItemDataType.LOGIN
                )
            }
            searchResultItemEditButton.setOnClickListener { onEditItemClicked.invoke(result) }
            searchResultItemDeleteButton.setOnClickListener { onDeleteItemClicked.invoke(result) }
            searchResultItemPasswordDesktopButton.setOnClickListener {
                sendToDesktop.invoke(
                    result,
                    ItemDataType.PASSWORD
                )
            }
            searchResultItemLoginDesktopButton.setOnClickListener {
                sendToDesktop.invoke(
                    result,
                    ItemDataType.LOGIN
                )
            }
        }
    }

    fun bindSelection(isSelected: Boolean) {
        toggleViewState(expand = isSelected)
    }

    private fun toggleViewState(expand: Boolean, animate: Boolean = true) {
        if (isAnimating) return
        itemView.doOnLayout {
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
                        val progress = (it.animatedValue as Float)
                        binding.searchResultItemCard.layoutParams.height =
                            currentHeight + (detailHeight.toFloat() * progress * coeff).toInt()
                        val padding =
                            currentSidePadding + (DETAIL_PADDING * progress * coeff).toInt()
                        binding.searchResultMainContainer.updatePadding(
                            left = padding,
                            right = padding
                        )
                        binding.searchResultItemCard.requestLayout()
                    }
                    doOnStart {
                        if (isExpanded) {
                            binding.searchResultItemDetail.fadeIn(animationDuration)
                        } else {
                            binding.searchResultItemDetail.fadeOut(animationDuration)
                        }
                    }
                    doOnEnd {
                        isAnimating = false
                        onAnimationEnd()
                    }
                }
                isAnimating = true
                anim.start()
            }
        }
    }

    private fun togglePasswordVisibility(password: String) {
        with(binding) {
            isPasswordVisible = !isPasswordVisible
            var text = view.context.getText(R.string.item_password)
            if (isPasswordVisible) {
                text = password
                searchResultItemPassword.transformationMethod = null
            } else {
                searchResultItemPassword.transformationMethod = PasswordTransformationMethod()
            }
            searchResultItemPassword.text = text
            searchResultItemPassword.requestLayout()
        }
    }

    companion object {
        private val DETAIL_HEIGHT_WITH_LOGIN = 220.px
        private val DETAIL_HEIGHT_WITHOUT_LOGIN = 150.px
        private val DETAIL_PADDING = (-10).px.toFloat()
        private const val ANIMATION_DURATION = 500L
    }

}
