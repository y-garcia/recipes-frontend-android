package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.*
import android.arch.persistence.room.ForeignKey.RESTRICT
import com.google.gson.annotations.SerializedName
import com.yeraygarcia.recipes.database.UUIDTypeConverter
import java.util.*

@Entity(
    tableName = "ingredient",
    indices = [Index(value = ["name"], unique = true), Index("aisle_id")],
    foreignKeys = [ForeignKey(
        entity = Aisle::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("aisle_id"),
        onDelete = RESTRICT
    )]
)
data class Ingredient(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var id: UUID = UUIDTypeConverter.newUUID(),

    var name: String,

    @ColumnInfo(name = "aisle_id", typeAffinity = ColumnInfo.BLOB)
    @SerializedName("aisle_id")
    var aisleId: UUID = UUID.fromString("7f8a3138-a072-11e8-9ac4-0a0027000012")
)
