package fr.jorisfavier.youshallnotpass.data.impl

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import fr.jorisfavier.youshallnotpass.data.IExternalItemDataSource
import fr.jorisfavier.youshallnotpass.data.model.ItemDto
import fr.jorisfavier.youshallnotpass.utils.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.util.*
import javax.inject.Inject

class ExternalItemDataSource @Inject constructor(
    private val appContext: Context,
    private val contentResolver: ContentResolver
) : IExternalItemDataSource {
    companion object {
        const val EXPORT_FOLDER = "exports"
        const val CSV_EXPORT_NAME = "ysnpExport.csv"
        const val YSNP_EXPORT_NAME = "export.ysnp"
        const val AUTHORITY = "fr.jorisfavier.fileprovider"
    }

    override suspend fun saveToCsv(items: List<ItemDto>): Uri {
        return withContext(Dispatchers.IO) {
            val res = StringBuilder()
            res.append("title,username,password\n")
            items.forEach {
                if (it.title != null && it.password != null) {
                    res.append("${it.title},${it.login.orEmpty()},${it.password}\n")
                }
            }
            val exportPath = appContext.getExternalFilesDir(EXPORT_FOLDER)
            val file = File(exportPath, CSV_EXPORT_NAME)
            val writer = FileWriter(file)
            writer.write(res.toString())
            writer.flush()
            writer.close()
            FileProvider.getUriForFile(appContext, AUTHORITY, file)
        }
    }

    override suspend fun saveToYsnpFile(data: ByteArray): Uri {
        return withContext(Dispatchers.IO) {
            val exportPath = appContext.getExternalFilesDir(EXPORT_FOLDER)
            val file = File(exportPath, YSNP_EXPORT_NAME)
            file.writeBytes(data)
            FileProvider.getUriForFile(appContext, AUTHORITY, file)
        }
    }

    override suspend fun isTextFile(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            val mimeType = contentResolver.getType(uri)
            return@withContext FileUtil.isTextFile(mimeType)
        }
    }

    override suspend fun getItemsFromTextFile(uri: Uri): List<ItemDto> {
        return withContext(Dispatchers.IO) {
            val items = mutableListOf<ItemDto>()
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var i = 0
                    var line: String? = reader.readLine()
                    var passwordIndex = 5 //default dashlane password position
                    var titleIndex = 0
                    var loginIndex = 2
                    var loginIndexBis = 2
                    while (line != null) {
                        val lineContent = line.split(",")
                        if (i == 0) {
                            lineContent
                                .asSequence()
                                .map {
                                    it.toLowerCase(Locale.ROOT)
                                        .replace("\"", "")
                                        .replace("'", "")
                                }
                                .forEachIndexed { index, header ->
                                    when {
                                        header == "title" -> titleIndex = index
                                        header == "name" -> titleIndex = index
                                        header.contains("pass") -> passwordIndex = index
                                        header.contains("username") -> loginIndex = index
                                        header == "login" -> loginIndex = index
                                    }
                                }
                        } else {
                            val login = lineContent.getOrNull(loginIndex) ?: lineContent.getOrNull(loginIndexBis)
                            items.add(ItemDto(lineContent.getOrNull(titleIndex), login, lineContent.getOrNull(passwordIndex)))
                        }
                        line = reader.readLine()
                        i++
                    }
                }
            }
            items
        }
    }

    override suspend fun getDataFromYsnpFile(uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            var bytes = ByteArray(0)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                bytes = inputStream.readBytes()
            }
            bytes
        }
    }
}