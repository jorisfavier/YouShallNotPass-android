package fr.jorisfavier.youshallnotpass.analytics.impl

import fr.jorisfavier.youshallnotpass.analytics.ScreenName
import fr.jorisfavier.youshallnotpass.analytics.YSNPAnalytics
import fr.jorisfavier.youshallnotpass.api.AnalyticsApi
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.model.Frequency
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

class YSNPAnalyticsImpl @Inject constructor(
    private val api: AnalyticsApi,
    private val appPreferenceDataSource: AppPreferenceDataSource,
) : YSNPAnalytics {

    override suspend fun trackScreenView(screen: ScreenName, frequency: Frequency?) {
        try {
            val shouldSendEvent = if (frequency != null) {
                appPreferenceDataSource.getAnalyticEventDate(screen)?.let { lastTimeEventWasSent ->
                    frequency.isOutdated(lastTimeEventWasSent)
                } ?: true
            } else {
                true
            }
            if (shouldSendEvent) {
                appPreferenceDataSource.setAnalyticEventDate(screen, LocalDateTime.now())
                api.sendEvent(screen.event)
            }
        } catch (e: Exception) {
            Timber.e(e, "Unable to send analytic event")
        }
    }
}
