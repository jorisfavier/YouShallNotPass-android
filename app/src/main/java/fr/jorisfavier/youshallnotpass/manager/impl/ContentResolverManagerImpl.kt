package fr.jorisfavier.youshallnotpass.manager.impl

import android.content.ContentResolver
import android.net.Uri
import fr.jorisfavier.youshallnotpass.manager.ContentResolverManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class ContentResolverManagerImpl(private val contentResolver: ContentResolver) : ContentResolverManager {
    override suspend fun getMimeType(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            return@withContext contentResolver.getType(uri)
        }
    }

    override suspend fun getFileBytes(uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            var bytes = ByteArray(0)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                bytes = inputStream.readBytes()
            }
            bytes
        }
    }

    override suspend fun getFileContent(uri: Uri): List<String> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<String>()
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        result.add(line)
                        line = reader.readLine()
                    }
                }
            }
            result
        }
    }

}