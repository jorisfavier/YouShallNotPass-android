package fr.jorisfavier.youshallnotpass.repository

interface DesktopRepository {
    suspend fun updateDesktopInfo(url: String, publicKey: String)
    suspend fun sendData(data: String)
}