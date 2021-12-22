package fr.jorisfavier.youshallnotpass.analytics

import fr.jorisfavier.youshallnotpass.model.Frequency

interface YSNPAnalytics {
    /**
     * Send a "screen view" event to the analytic system
     * @param screen the screen name that should be recorded
     * @param frequency indicates how often this event should be sent, if null the event will be sent
     */
    suspend fun trackScreenView(screen: ScreenName, frequency: Frequency? = null)
}