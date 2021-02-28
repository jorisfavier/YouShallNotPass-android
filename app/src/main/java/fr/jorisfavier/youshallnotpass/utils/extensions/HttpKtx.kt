package fr.jorisfavier.youshallnotpass.utils.extensions

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

fun HttpUrl.Builder.hostWithPort(stringUrl: String) = apply {
    stringUrl.toHttpUrlOrNull()?.let {
        host(it.host)
        port(it.port)
    }
}