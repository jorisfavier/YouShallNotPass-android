package fr.jorisfavier.youshallnotpass.repository.mapper

import fr.jorisfavier.youshallnotpass.data.model.ItemDto
import fr.jorisfavier.youshallnotpass.model.ExternalItem

object DtoToModel {
    fun itemDtoListToExternalItemList(itemDto: List<ItemDto>): List<ExternalItem> {
        return itemDto.asSequence()
            .filter { it.title != null && it.password != null }
            .map { ExternalItem(it.title!!, it.login, it.password!!) }.toList()
    }
}