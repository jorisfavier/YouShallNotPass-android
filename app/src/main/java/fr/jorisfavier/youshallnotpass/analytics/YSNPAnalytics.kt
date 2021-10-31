package fr.jorisfavier.youshallnotpass.analytics

interface YSNPAnalytics {
    suspend fun trackScreenView(screen: ScreenName)
}