package fr.jorisfavier.youshallnotpass.manager

import android.net.Uri

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
}