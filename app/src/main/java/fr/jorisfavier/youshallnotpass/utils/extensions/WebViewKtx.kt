package fr.jorisfavier.youshallnotpass.utils.extensions

import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

fun WebView.forceDarkMode(isDarkModeOn: Boolean) {
    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
        val darkModeSettings = if (isDarkModeOn) {
            WebSettingsCompat.FORCE_DARK_ON
        } else {
            WebSettingsCompat.FORCE_DARK_OFF
        }
        WebSettingsCompat.setForceDark(settings, darkModeSettings)
    }
    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
        WebSettingsCompat.setForceDarkStrategy(
            settings,
            WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING
        )
    }
}