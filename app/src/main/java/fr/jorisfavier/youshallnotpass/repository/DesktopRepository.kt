package fr.jorisfavier.youshallnotpass.repository

interface DesktopRepository {
    suspend fun updateDesktopInfo(url: String, publicKey: String): Result<Unit>
    suspend fun sendData(data: String): Result<Unit>
}
