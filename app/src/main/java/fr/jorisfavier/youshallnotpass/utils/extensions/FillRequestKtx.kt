package fr.jorisfavier.youshallnotpass.utils.extensions

import android.os.Build
import android.service.autofill.FillRequest
import android.widget.inline.InlinePresentationSpec
import androidx.autofill.inline.UiVersions

val FillRequest.inlinePresentationSpec: InlinePresentationSpec?
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val imeSpec = inlineSuggestionsRequest?.inlinePresentationSpecs?.firstOrNull()
            // Make sure that the IME spec claims support for v1 UI template.
            if (imeSpec != null
                && UiVersions.getVersions(imeSpec.style).contains(UiVersions.INLINE_UI_VERSION_1)
            ) {
                imeSpec
            } else {
                null
            }
        } else {
            null
        }
    }