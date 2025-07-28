package fr.jorisfavier.youshallnotpass.utils.extensions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

@ColorInt
fun Context.getThemeColor(
    @AttrRes attrColor: Int,
): Int {
    val resolvedAttr = TypedValue().apply {
        theme.resolveAttribute(attrColor, this, true)
    }
    // resourceId is used if it's a ColorStateList, and data if it's a color reference or a hex color
    return if (resolvedAttr.resourceId != 0) {
        ContextCompat.getColor(this, resolvedAttr.resourceId)
    } else {
        resolvedAttr.data
    }
}

fun Context.toast(@StringRes stringResId: Int) {
    Toast.makeText(this, stringResId, Toast.LENGTH_LONG).show()
}

fun Context.isDarkMode(): Boolean {
    val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return uiMode == Configuration.UI_MODE_NIGHT_YES
}

fun Context.openEmail(email: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    }
    if (packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .isNotEmpty()
    ) {
        startActivity(intent)
    }
}
