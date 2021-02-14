package fr.jorisfavier.youshallnotpass.repository.impl

import android.util.Base64
import fr.jorisfavier.youshallnotpass.api.DesktopApi
import fr.jorisfavier.youshallnotpass.api.HostInterceptor
import fr.jorisfavier.youshallnotpass.data.AppPreferenceDataSource
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.repository.DesktopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class DesktopRepositoryImpl(
    private val api: DesktopApi,
    private val appPreference: AppPreferenceDataSource,
    private val hostInterceptor: HostInterceptor,
    private val cryptoManager: ICryptoManager
) : DesktopRepository {

    override suspend fun updateDesktopInfo(url: String, publicKey: String): Result<Unit> = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            appPreference.setDesktopAddress(url)
            hostInterceptor.host = url
            appPreference.setDesktopPublicKey(publicKey)
            Timber.d(publicKey)
        }
    }

    override suspend fun sendData(data: String): Result<Unit> = kotlin.runCatching {
        val key = appPreference.getDesktopPublicKey()
        val encrypted = cryptoManager.encryptDataWithPublicKey(key!!, data)
        api.sendData(Base64.encodeToString(encrypted, Base64.DEFAULT))
    }

}