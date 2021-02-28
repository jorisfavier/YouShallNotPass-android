package fr.jorisfavier.youshallnotpass.utils.extensions

import android.content.Context
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes

@ColorInt
fun Context.getThemeColor(
    @AttrRes attrColor: Int,
): Int {
    return TypedValue().let {
        theme.resolveAttribute(attrColor, it, true)
        it.data
    }
}

fun Context.toast(@StringRes stringResId: Int) {
    Toast.makeText(this, stringResId, Toast.LENGTH_LONG).show()
}
