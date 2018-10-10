package com.yeraygarcia.recipes.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(
    tableName = "unit",
    indices = [Index(value = ["name_singular"], unique = true), Index(
        value = ["name_plural"],
        unique = true
    )]
)
data class Unit(
    @PrimaryKey
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var id: UUID,

    @ColumnInfo(name = "name_singular")
    @SerializedName("name_singular")
    var nameSingular: String,

    @ColumnInfo(name = "name_plural")
    @SerializedName("name_plural")
    var namePlural: String
)
