package fr.jorisfavier.youshallnotpass.api

import fr.jorisfavier.youshallnotpass.utils.extensions.hostWithPort
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HostInterceptor @Inject constructor() : Interceptor {

    @Volatile
    var host: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        host?.let {
            val url = request.url.newBuilder().hostWithPort(it).build()
            request = request.newBuilder().url(url).build()
        }
        return chain.proceed(request)
    }
}