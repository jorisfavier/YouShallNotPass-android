package fr.jorisfavier.youshallnotpass.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

/**
 * Switch the view to visible with a fade animation
 *
 * @param animationDuration the animation duration,
 * by default it will be set to android.R.integer.config_shortAnimTime
 */
fun View.fadeIn(
        animationDuration: Long = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
)
{
    alpha = 0f
    visibility = View.VISIBLE
    // Animate the content view to 100% opacity, and clear any animation
    // listener set on the view.
    animate()
            .alpha(1f)
            .setDuration(animationDuration)
            .setListener(null)
}

/**
 * Set the view to GONE with a fade animation
 *
 * @param animationDuration the animation duration,
 * by default it will be set to android.R.integer.config_shortAnimTime
 */
fun View.fadeOut(
        animationDuration: Long = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
)
{
    // Animate the content view to 100% opacity, and clear any animation
    // listener set on the view.
    animate()
            .alpha(0f)
            .setDuration(animationDuration)
            .setListener(
                    object : AnimatorListenerAdapter()
                    {
                        override fun onAnimationEnd(animation: Animator?)
                        {
                            visibility = View.GONE
                            super.onAnimationEnd(animation)
                        }
                    }
            )
}