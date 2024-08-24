package fr.jorisfavier.youshallnotpass.utils.extensions

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

fun HttpUrl.Builder.hostWithPort(stringUrl: String) = apply {
    val httpUrl = stringUrl.toHttpUrlOrNull() ?: return@apply
    host(httpUrl.host)
    port(httpUrl.port)
}
