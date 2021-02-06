package fr.jorisfavier.youshallnotpass.data

import android.net.Uri
import fr.jorisfavier.youshallnotpass.data.model.ItemDto

interface ExternalItemDataSource {
    /**
     * Save the given list of ItemDto to a csv file
     * @param items
     * @return the path of the csv file
     */
    suspend fun saveToCsv(items: List<ItemDto>): Uri

    /**
     * Save the given data to a file with the .ysnp extension
     * @param data
     * @return the path of the ysnp file
     */
    suspend fun saveToYsnpFile(data: ByteArray): Uri

    /**
     * Indicates if a file contains readable text or not
     * @param uri the uri to the file
     * @return true if the file is readable
     */
    suspend fun isTextFile(uri: Uri): Boolean

    /**
     * Retrieve the possible items from the given text file
     * @param uri the uri to the text file
     * @return List<ItemDto>
     */
    suspend fun getItemsFromTextFile(uri: Uri): List<ItemDto>

    /**
     * Retrieve the content of a secure ysnp file
     * @param uri the uri to the ysnp file
     * @return ByteArray the content of the file
     */
    suspend fun getDataFromYsnpFile(uri: Uri): ByteArray
}