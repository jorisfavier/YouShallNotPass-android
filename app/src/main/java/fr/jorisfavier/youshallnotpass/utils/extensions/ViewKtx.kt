package fr.jorisfavier.youshallnotpass.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.camera.core.AspectRatio
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val RATIO_4_3_VALUE = 4.0 / 3.0
private const val RATIO_16_9_VALUE = 16.0 / 9.0

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
) {
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
) {
    // Animate the content view to 100% opacity, and clear any animation
    // listener set on the view.
    animate()
        .alpha(0f)
        .setDuration(animationDuration)
        .setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    visibility = View.GONE
                    super.onAnimationEnd(animation)
                }
            }
        )
}

fun View.aspectRatio(): Int {
    val metrics = DisplayMetrics().also { display.getRealMetrics(it) }
    return aspectRatio(metrics.widthPixels, metrics.heightPixels)
}

/**
 *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
 *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
 *
 *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
 *  of preview ratio to one of the provided values.
 *
 *  @param width - preview width
 *  @param height - preview height
 *  @return suitable aspect ratio
 */
private fun aspectRatio(width: Int, height: Int): Int {
    val previewRatio = max(width, height).toDouble() / min(width, height)
    if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
        return AspectRatio.RATIO_4_3
    }
    return AspectRatio.RATIO_16_9
}