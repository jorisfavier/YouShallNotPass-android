package fr.jorisfavier.youshallnotpass.utils.extensions

import java.security.MessageDigest
import java.util.*


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
    if (startsWith("http") || startsWith("www")) {
        val segments = split(".")
        return when (segments.size) {
            2 -> segments.first()
                .removePrefix("http://")
                .removePrefix("https://")
            3 -> segments[1]
            else -> segments.first()
        }
    }
    return this
}

/**
 * Returns a copy of this string having its first letter titlecased using the rules of the specified locale,
 * or the original string if it's empty or already starts with a title case letter.
 */
fun String.titleCase(locale: Locale = Locale.getDefault()): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
    }
}