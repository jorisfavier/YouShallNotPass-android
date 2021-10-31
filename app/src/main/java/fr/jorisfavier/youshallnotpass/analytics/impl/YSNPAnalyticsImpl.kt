package fr.jorisfavier.youshallnotpass.analytics.impl

import fr.jorisfavier.youshallnotpass.analytics.ScreenName
import fr.jorisfavier.youshallnotpass.analytics.YSNPAnalytics
import fr.jorisfavier.youshallnotpass.api.AnalyticsApi
import timber.log.Timber
import javax.inject.Inject

class YSNPAnalyticsImpl @Inject constructor(
    private val api: AnalyticsApi,
) : YSNPAnalytics {

    override suspend fun trackScreenView(screen: ScreenName) {
        try {
            api.sendEvent(screen.event)
        } catch (e: Exception) {
            Timber.e(e, "Unable to send analytic event")
        }
    }
}