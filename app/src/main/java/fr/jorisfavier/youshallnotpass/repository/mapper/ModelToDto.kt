package fr.jorisfavier.youshallnotpass.repository.mapper

import fr.jorisfavier.youshallnotpass.data.model.ItemDto
import fr.jorisfavier.youshallnotpass.model.ExternalItem

object ModelToDto {
    fun externalItemToItemDto(externalItem: ExternalItem): ItemDto {
        return ItemDto(externalItem.title, externalItem.password)
    }
}