package fr.jorisfavier.youshallnotpass.utils.extensions

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.os.PersistableBundle

/**
 * Flag the current [ClipData] as sensitive content,
 * add a boolean extra to the [ClipDescription]
 */
fun ClipData.addFlagSensitiveData() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        description.extras = PersistableBundle().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            } else {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }
    }
}
