package fr.jorisfavier.youshallnotpass.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Item(
        @PrimaryKey
        val id: Int,
        @ColumnInfo(name = "title")
        val title: String)