package fr.jorisfavier.youshallnotpass.repository.impl

import android.util.Base64
import fr.jorisfavier.youshallnotpass.api.DesktopApi
import fr.jorisfavier.youshallnotpass.api.HostInterceptor
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.repository.DesktopRepository
import fr.jorisfavier.youshallnotpass.utils.extensions.suspendRunCatching

class DesktopRepositoryImpl(
    private val api: DesktopApi,
    private val appPreference: AppPreferenceDataSource,
    private val hostInterceptor: HostInterceptor,
    private val cryptoManager: CryptoManager,
) : DesktopRepository {

    override suspend fun updateDesktopInfo(url: String, publicKey: String): Result<Unit> {
        return suspendRunCatching(
            errorMessage = "An error occurred while updating the desktop info",
        ) {
            appPreference.setDesktopAddress(url)
            hostInterceptor.host = url
            appPreference.setDesktopPublicKey(publicKey)
        }
    }

    override suspend fun sendData(data: String): Result<Unit> {
        return suspendRunCatching(
            errorMessage = "An error occurred while sending data to the desktop app",
        ) {
            val key = appPreference.getDesktopPublicKey()
            val encrypted = cryptoManager.encryptDataWithPublicKey(key!!, data).getOrThrow()
            api.sendData(Base64.encodeToString(encrypted, Base64.DEFAULT))
        }
    }

}
