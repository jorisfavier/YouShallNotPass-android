package fr.jorisfavier.youshallnotpass.ui.common

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import fr.jorisfavier.youshallnotpass.utils.extensions.openEmail

class WebViewWithExtraClient : WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?,
    ): Boolean {
        val context = view?.context ?: return super.shouldOverrideUrlLoading(view, request)
        val url = request?.url?.toString()
        return if (url != null && url.startsWith(EMAIL_SCHEME)) {
            context.openEmail(url.replace(EMAIL_SCHEME, ""))
            true
        } else {
            super.shouldOverrideUrlLoading(view, request)
        }
    }

    companion object {
        private const val EMAIL_SCHEME = "mailto:"
    }
}