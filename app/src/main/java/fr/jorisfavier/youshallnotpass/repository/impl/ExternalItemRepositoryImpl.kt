package fr.jorisfavier.youshallnotpass.repository.impl

import android.net.Uri
import fr.jorisfavier.youshallnotpass.data.ExternalItemDataSource
import fr.jorisfavier.youshallnotpass.data.model.ItemDto
import fr.jorisfavier.youshallnotpass.manager.CryptoManager
import fr.jorisfavier.youshallnotpass.model.ExternalItem
import fr.jorisfavier.youshallnotpass.repository.ExternalItemRepository
import fr.jorisfavier.youshallnotpass.repository.mapper.DtoToModel
import fr.jorisfavier.youshallnotpass.repository.mapper.ModelToDto
import fr.jorisfavier.youshallnotpass.utils.extensions.suspendRunCatching
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ExternalItemRepositoryImpl(
    private val externalItemDataSource: ExternalItemDataSource,
    private val cryptoManager: CryptoManager,
) : ExternalItemRepository {

    override suspend fun saveExternalItems(
        items: List<ExternalItem>,
        password: String?,
    ): Result<Uri> {
        return suspendRunCatching(
            errorMessage = "An error occurred while saving external items",
        ) {
            val itemsToSave = items.map { ModelToDto.externalItemToItemDto(it) }
            if (password != null) {
                val itemsJson = Json.encodeToString(itemsToSave)
                val data = cryptoManager.encryptDataWithPassword(
                    password = password,
                    data = itemsJson.toByteArray(Charsets.UTF_8),
                ).getOrThrow()
                externalItemDataSource.saveToYsnpFile(data)
            } else {
                externalItemDataSource.saveToCsv(itemsToSave)
            }
        }
    }


    override suspend fun getExternalItemsFromUri(
        uri: Uri,
        password: String?,
    ): Result<List<ExternalItem>> {
        return suspendRunCatching(
            errorMessage = "An error occurred while retrieving items from $uri",
        ) {
            val items = if (password != null) {
                val encryptedData = externalItemDataSource.getDataFromYsnpFile(uri)
                val decrypted =
                    cryptoManager.decryptDataWithPassword(password, encryptedData).getOrThrow()
                Json.decodeFromString<List<ItemDto>>(decrypted.toString(Charsets.UTF_8))
            } else {
                externalItemDataSource.getItemsFromTextFile(uri)
            }
            DtoToModel.itemDtoListToExternalItemList(items)
        }
    }

    override suspend fun isSecuredWithPassword(uri: Uri): Result<Boolean> {
        return suspendRunCatching(
            errorMessage = "An error occurred while checking if the file from $uri is secured",
        ) {
            !externalItemDataSource.isTextFile(uri)
        }
    }
}
