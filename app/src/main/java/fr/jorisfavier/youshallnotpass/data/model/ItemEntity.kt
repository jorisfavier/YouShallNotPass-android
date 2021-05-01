package fr.jorisfavier.youshallnotpass.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "login")
    val login: String? = null,
    @ColumnInfo(name = "password", typeAffinity = ColumnInfo.BLOB)
    val password: ByteArray,
    @ColumnInfo(name = "initializationVector", typeAffinity = ColumnInfo.BLOB)
    val initializationVector: ByteArray,
    @ColumnInfo(name = "packageCertificates")
    val packageCertificates: String? = null,
)