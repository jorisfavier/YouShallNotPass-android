package fr.jorisfavier.youshallnotpass.manager

import android.net.Uri

interface ContentResolverManager {
    /**
     * Gets the mimeType of a file
     * @param uri the Uri to the file
     * @return the MimeType as a String or null if the file hasn't been found
     */
    suspend fun getMimeType(uri: Uri): String?

    /**
     * Gives the content of a file as ByteArray
     * @param uri the Uri to the file
     * @return the content of the file as ByteArray
     */
    suspend fun getFileBytes(uri: Uri): ByteArray

    /**
     * Gives the content of a file as String lines
     * @param uri the Uri to the file
     * @return the content of the file as a list of String
     */
    suspend fun getFileContent(uri: Uri): List<String>
}