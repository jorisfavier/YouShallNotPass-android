package fr.jorisfavier.youshallnotpass.api

import retrofit2.http.Body
import retrofit2.http.POST

interface DesktopApi {

    /**
     * Send data to the desktop app
     * @param data the data to send
     */
    @POST("message")
    suspend fun sendData(@Body data: String)

}