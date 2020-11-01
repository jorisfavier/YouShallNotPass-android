package fr.jorisfavier.youshallnotpass.manager

import android.net.Uri
import fr.jorisfavier.youshallnotpass.model.ImportedItem

interface IFileManager {
    /**
     * Save the given data to a csv file
     * @param data
     * @return the path of the file
     */
    suspend fun saveToCsv(data: String): Uri

    /**
     * Save the given data to a file with the .ysnp extension
     * @param data
     * @return the path of the file
     */
    suspend fun saveToYsnpFile(data: ByteArray): Uri

    /**
     * Indicates if a file contains readable text or not
     * @param uri the uri to the file
     * @return true if the file is readable
     */
    suspend fun isTextFile(uri: Uri): Boolean

    /**
     * Retrieve the possible passwords from the given text file
     * @param uri the uri to the text file
     * @return List<ImportedItem>
     */
    suspend fun getImportedItemsFromTextFile(uri: Uri): List<ImportedItem>

    /**
     * Retrieve the content of a secure ysnp file
     * @param uri the uri to the ysnp file
     * @return ByteArray the content of the file
     */
    suspend fun getDataFromYsnpFile(uri: Uri): ByteArray
}