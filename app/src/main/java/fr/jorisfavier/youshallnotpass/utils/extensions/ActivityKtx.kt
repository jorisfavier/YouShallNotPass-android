package fr.jorisfavier.youshallnotpass.utils.extensions

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.fragment.NavHostFragment

fun FragmentActivity?.hideKeyboard() {
    val imm =
        this?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Find the currently focused view, so we can grab the correct window token from it.
    var view = this.currentFocus
    // If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * Find a [NavController] given the id of a [FragmentContainerView] and its containing
 * [Activity].
 *
 * Calling this on a View that is not a [NavHost] or within a [NavHost]
 * will result in an [IllegalStateException]
 */
fun AppCompatActivity.findNavControllerFromFragmentContainerView(@IdRes viewId: Int): NavController {
    val navHost =
        supportFragmentManager.findFragmentById(viewId) as NavHostFragment
    return navHost.navController
}