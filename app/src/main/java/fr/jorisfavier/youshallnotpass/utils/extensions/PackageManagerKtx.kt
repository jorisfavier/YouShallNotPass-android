package fr.jorisfavier.youshallnotpass.utils.extensions

import android.content.pm.PackageManager
import okio.ByteString

/**
 * Gets the certificates from a given [packageName]
 * @param packageName
 * @return a list of hashed (sha256) certificates found for the given [packageName] or an empty list
 * if nothing was found.
 */
fun PackageManager.getCertificateHashes(packageName: String): List<String> {
    return runCatching {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val packageInfo = getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signingInfo = packageInfo.signingInfo!!
            if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners.map {
                    ByteString.of(*it.toByteArray()).sha256().hex()
                }
            } else {
                signingInfo.signingCertificateHistory.map {
                    ByteString.of(*it.toByteArray()).sha256().hex()
                }
            }
        } else {
            val packageInfo = getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            val signatures = packageInfo.signatures!!
            signatures.map {
                ByteString.of(*it.toByteArray()).sha1().hex()
            }
        }
    }.getOrDefault(listOf())
}

/**
 * Retrieves the app name for a given [packageName].
 * @param packageName
 * @return the app name or null if not found
 */
fun PackageManager.getAppName(packageName: String): String? {
    return try {
        val appInfo = getApplicationInfo(packageName, 0)
        getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        null
    }
}
