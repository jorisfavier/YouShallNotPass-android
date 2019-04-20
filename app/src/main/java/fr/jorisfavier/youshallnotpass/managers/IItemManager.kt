package fr.jorisfavier.youshallnotpass.managers

import fr.jorisfavier.youshallnotpass.models.Item

interface IItemManager {
    fun searchItem(title: String): List<Item>
}