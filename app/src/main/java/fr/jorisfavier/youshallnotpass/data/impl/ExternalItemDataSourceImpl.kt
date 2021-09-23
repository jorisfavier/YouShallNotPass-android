package fr.jorisfavier.youshallnotpass.data.impl

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.data.ExternalItemDataSource
import fr.jorisfavier.youshallnotpass.data.model.ItemDto
import fr.jorisfavier.youshallnotpass.manager.ContentResolverManager
import fr.jorisfavier.youshallnotpass.model.exception.YsnpException
import fr.jorisfavier.youshallnotpass.utils.FileUtil
import fr.jorisfavier.youshallnotpass.utils.extensions.firstNotNull
import fr.jorisfavier.youshallnotpass.utils.extensions.getDomainIfUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.util.*
import javax.inject.Inject

class ExternalItemDataSourceImpl @Inject constructor(
    private val appContext: Context,
    private val contentResolver: ContentResolverManager
) : ExternalItemDataSource {

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
            val mimeType = contentResolver.getMimeType(uri)
            return@withContext FileUtil.isTextFile(mimeType)
        }
    }

    override suspend fun getItemsFromTextFile(uri: Uri): List<ItemDto> {
        return withContext(Dispatchers.IO) {
            val items = mutableListOf<ItemDto>()
            val fileContent = contentResolver.getFileContent(uri)
            var passwordIndex: Int? = null
            var titleIndex: Int? = null
            var loginIndex: Int? = null
            var urlIndex: Int? = null
            fileContent.forEachIndexed { i, line ->
                val lineContent = line
                    .split(",", "\n")
                    .map { it.removePrefix("\"").removeSuffix("\"") }
                if (i == 0) {
                    lineContent
                        .asSequence()
                        .map { it.toLowerCase(Locale.ROOT) }
                        .forEachIndexed { index, header ->
                            when {
                                header == "title" -> titleIndex = index
                                header == "name" -> titleIndex = index
                                header == "login_password" -> passwordIndex = index
                                header.contains("username") -> loginIndex = index
                                header == "login" -> loginIndex = index
                                header == "password" -> passwordIndex = index
                                header == "url" -> urlIndex = index
                            }
                        }
                    if (titleIndex == null && passwordIndex == null && loginIndex == null) {
                        throw YsnpException(R.string.error_no_header_found)
                    }
                } else {
                    items.add(
                        ItemDto(
                            title = lineContent
                                .getOrNull(
                                    firstNotNull(
                                        defaultValue = DEFAULT_TITLE_INDEX,
                                        titleIndex,
                                        urlIndex
                                    )
                                )
                                ?.getDomainIfUrl()
                                ?.capitalize(Locale.getDefault()),
                            login = lineContent.getOrNull(
                                firstNotNull(
                                    defaultValue = DEFAULT_LOGIN_INDEX,
                                    loginIndex
                                )
                            ),
                            password = lineContent.getOrNull(
                                firstNotNull(
                                    defaultValue = DEFAULT_PASSWORD_INDEX,
                                    passwordIndex
                                )
                            ),
                        )
                    )
                }
            }
            items
        }
    }

    override suspend fun getDataFromYsnpFile(uri: Uri): ByteArray {
        return contentResolver.getFileBytes(uri)
    }

    companion object {
        private const val EXPORT_FOLDER = "exports"
        private const val CSV_EXPORT_NAME = "ysnpExport.csv"
        private const val YSNP_EXPORT_NAME = "export.ysnp"
        private const val AUTHORITY = "fr.jorisfavier.fileprovider"
        private const val DEFAULT_LOGIN_INDEX = 1
        private const val DEFAULT_PASSWORD_INDEX = 2
        private const val DEFAULT_TITLE_INDEX = 0
    }
}