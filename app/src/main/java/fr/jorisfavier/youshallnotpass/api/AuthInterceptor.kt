package fr.jorisfavier.youshallnotpass.api

import fr.jorisfavier.youshallnotpass.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newUrl = chain
            .request()
            .url
            .newBuilder()
            .addQueryParameter("p", BuildConfig.ANALYTIC_ID)
            .build()

        val requestBuilder = chain.request().newBuilder().url(newUrl)
        val request = requestBuilder.build()
        return chain.proceed(request)
    }

}