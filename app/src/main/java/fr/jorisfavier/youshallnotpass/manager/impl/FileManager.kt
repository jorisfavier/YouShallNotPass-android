package fr.jorisfavier.youshallnotpass.manager.impl

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import fr.jorisfavier.youshallnotpass.manager.IFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

class FileManager @Inject constructor(private var appContext: Context) : IFileManager {
    companion object {
        const val EXPORT_FOLDER = "exports"
        const val CSV_EXPORT_NAME = "ysnpExport.csv"
        const val YSNP_EXPORT_NAME = "export.ysnp"
        const val AUTHORITY = "fr.jorisfavier.fileprovider"
    }

    override suspend fun saveToCsv(data: String): Uri {
        return withContext(Dispatchers.IO) {
            val exportPath = appContext.getExternalFilesDir(EXPORT_FOLDER)
            val file = File(exportPath, CSV_EXPORT_NAME)
            val writer = FileWriter(file)
            writer.write(data)
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

}