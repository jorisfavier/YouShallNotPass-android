package fr.jorisfavier.youshallnotpass.utils.extensions

import java.security.MessageDigest

fun String.md5(): ByteArray? {
    val md = MessageDigest.getInstance("MD5");
    return try {
        md.update(this.toByteArray())
        md.digest()
    } catch (e: Exception) {
        null
    }
}

/**
 * If the current strings starts as a web Url it will try to extract the domain name
 * @return the url domain or the string itself if it's not a url
 */
fun String.getDomainIfUrl(): String {
    if (startsWith("http") || startsWith("wwww")) {
        val segments = split(".")
        return if (segments.size == 2) {
            segments.first()
                .removePrefix("http://")
                .removePrefix("http://www")
                .removePrefix("https://")
                .removePrefix("https://wwww")
        } else segments.getOrElse(1) { segments.first() }
    }
    return this
}