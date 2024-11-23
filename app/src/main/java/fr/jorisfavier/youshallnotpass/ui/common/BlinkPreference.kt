package fr.jorisfavier.youshallnotpass.ui.common

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import fr.jorisfavier.youshallnotpass.R

/**
 * Preference element exposing a blink method that allow to play a blink animation
 */
class BlinkPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : Preference(context, attrs) {

    private var blink = false

    /**
     * Play a blink animation on the element. The animation will use the background drawable to play,
     * it will play only once and the item will end up with a lighter background to put an emphasis on it.
     * Beware that the ripple effect will be removed.
     */
    fun blink() {
        blink = true
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        if (blink) {
            holder.itemView.apply {
                setBackgroundResource(R.drawable.blink)
                (background as? AnimationDrawable)?.start()
            }
        }
    }
}
