package fr.jorisfavier.youshallnotpass.repository.impl

import android.net.Uri
import fr.jorisfavier.youshallnotpass.data.ExternalItemDataSource
import fr.jorisfavier.youshallnotpass.data.model.ItemDto
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.repository.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.mapper.DtoToModel
import fr.jorisfavier.youshallnotpass.repository.mapper.ModelToDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ExternalItemRepositoryImpl(
    private val externalItemDataSource: ExternalItemDataSource,
    private val cryptoManager: CryptoManager
) : ExternalItemRepository {

    override suspend fun saveExternalItems(items: List<ExternalItem>, password: String?): Uri {
        return withContext(Dispatchers.IO) {
            val itemsToSave = items.map { ModelToDto.externalItemToItemDto(it) }
            password?.let {
                val itemsJson = Json.encodeToString(itemsToSave)
                val data = cryptoManager.encryptDataWithPassword(password, itemsJson.toByteArray(Charsets.UTF_8))
                return@withContext externalItemDataSource.saveToYsnpFile(data)
            } ?: run {
                return@withContext externalItemDataSource.saveToCsv(itemsToSave)
            }
        }
    }


    override suspend fun getExternalItemsFromUri(uri: Uri, password: String?): List<ExternalItem> {
        return withContext(Dispatchers.IO) {
            val items = password?.let {
                val encryptedData = externalItemDataSource.getDataFromYsnpFile(uri)
                val decrypted = cryptoManager.decryptDataWithPassword(password, encryptedData)
                Json.decodeFromString<List<ItemDto>>(decrypted.toString(Charsets.UTF_8))
            } ?: run {
                externalItemDataSource.getItemsFromTextFile(uri)
            }
            return@withContext DtoToModel.itemDtoListToExternalItemList(items)
        }
    }

    override suspend fun isSecuredWithPassword(uri: Uri): Boolean = !externalItemDataSource.isTextFile(uri)
}