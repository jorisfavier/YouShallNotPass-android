package fr.jorisfavier.youshallnotpass.repository

import android.net.Uri
import fr.jorisfavier.youshallnotpass.model.ExternalItem

interface ExternalItemRepository {

    /**
     * Persist a given list of ExternalItem by encrypting it with the given password
     * @param items the list of ExternalItem to persist
     * @param password the password used to encrypt the list
     * @return Uri the place where the data are stored
     */
    suspend fun saveExternalItems(items: List<ExternalItem>, password: String? = null): Uri

    /**
     * Retrieves a list of ExternalItem by decrypting it with a given password
     * @param uri the place where the data are stored
     * @param password the key to decrypt the data
     * @return List<ExternalItem>
     */
    suspend fun getExternalItemsFromUri(uri: Uri, password: String?): List<ExternalItem>

    /**
     * Check if data at a given uri are encrypted or not
     * @param uri the place where to find the data
     * @return Boolean true if the data are encrypted
     */
    suspend fun isSecuredWithPassword(uri: Uri): Boolean
}