package fr.jorisfavier.youshallnotpass.utils

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