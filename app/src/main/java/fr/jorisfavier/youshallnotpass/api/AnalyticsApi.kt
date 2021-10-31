package fr.jorisfavier.youshallnotpass.api

import retrofit2.http.GET
import retrofit2.http.Query

interface AnalyticsApi {
    @GET("/ship")
    suspend fun sendEvent(@Query("i") identifier: String)
}